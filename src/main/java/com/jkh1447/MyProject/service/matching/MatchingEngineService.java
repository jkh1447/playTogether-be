package com.jkh1447.MyProject.service.matching;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import com.jkh1447.MyProject.domain.matching.exception.MatchingErrorCode;
import com.jkh1447.MyProject.domain.matching.exception.UserQueueInfoParsingException;
import com.jkh1447.MyProject.dto.matching.MatchDeclineResponse;
import com.jkh1447.MyProject.dto.matching.MatchParticipant;
import com.jkh1447.MyProject.dto.matching.QueueInfo;
import com.jkh1447.MyProject.dto.matching.MatchStatusInfo;
import com.jkh1447.MyProject.service.matching.strategy.MatchStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.jkh1447.MyProject.dto.matching.QueueUser;
import com.jkh1447.MyProject.service.matching.strategy.MatchStrategy;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingEngineService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    private final MatchingNotificationService notificationService;
    private final MatchQueueService queueService;
    private final UserInfoHelper userInfoHelper;
    private final MatchStatusService matchStatusService;
    private final ScheduledExecutorService matchTimeoutExecutor;
    private final MatchStrategyFactory strategyFactory;
    private final ObjectMapper objectMapper;

    public void processMatching() {
        Set<Object> activeQueues =
                redisTemplate.opsForSet().members(MatchingConstants.ACTIVE_QUEUES_KEY);

        Set<Object> activeAnyQueues =
                redisTemplate.opsForSet().members(MatchingConstants.ACTIVE_ANY_QUEUE_KEY);

        // 인원 조건 매칭 먼저 -> 인원 조건이 없는 매칭
        if ((activeQueues == null || activeQueues.isEmpty()) && (activeAnyQueues == null || activeAnyQueues.isEmpty())) {
            return;
        }

        for (Object queueKeyObj : activeQueues) {
            String queueKey = (String) queueKeyObj;
            processQueue(queueKey);
        }

        for (Object queueKeyObj : activeAnyQueues) {
            String queueKey = (String) queueKeyObj;
            processAnyQueue(queueKey);
        }
    }

    private void processQueue(String queueKey) { // 매칭

        RLock lock = redissonClient.getLock("lock:" + queueKey);

        try {
            boolean isLocked = lock.tryLock(0, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                return;
            }

            QueueInfo queueInfo = QueueInfo.fromQueueKey(queueKey);
            log.info("[매칭] queueKey: {}", queueKey);
            MatchStrategy strategy = strategyFactory.getStrategy(queueInfo.getGameName());
            String anyQueueKey = strategy.generateAnyQueueKey(queueKey);

            List<QueueUser> selfUsers =
                    queueService.loadFromQueue(queueKey, MatchingConstants.SELF_LIMIT);
            List<QueueUser> anyUsers =
                    queueService.loadFromQueue(anyQueueKey, MatchingConstants.ANY_LIMIT);

            List<QueueUser> totalPool = queueService.combineAndSortPool(selfUsers, anyUsers);

            List<QueueUser> pivots =
                    selfUsers.stream().limit(MatchingConstants.PIVOT_LIMIT).toList();

            for (QueueUser pivot : pivots) {
                List<QueueUser> finalTeam = strategy.buildTeam(pivot, totalPool, queueInfo);
                if (finalTeam.size() == Integer.parseInt(queueInfo.getGroupSize())) {
                    attemptMatch(queueKey, queueInfo, finalTeam);
                    return;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (IllegalMonitorStateException e) {
                // 락이 이미 만료된 상황. 
                log.error("매칭 엔진 로직이 제한 시간(10초)을 초과했습니다");
            }
        }

    }

    private void processAnyQueue(String queueKey) { // 매칭

        RLock lock = redissonClient.getLock("lock:" + queueKey);

        try {
            boolean isLocked = lock.tryLock(0, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                return;
            }

            QueueInfo queueInfo = QueueInfo.fromQueueKey(queueKey);
            MatchStrategy strategy = strategyFactory.getStrategy(queueInfo.getGameName());

            List<QueueUser> totalPool =
                    queueService.loadFromQueue(queueKey, MatchingConstants.ONLY_ANY_LIMIT);

            List<QueueUser> pivots =
                    totalPool.stream().limit(MatchingConstants.PIVOT_LIMIT).toList();

            for (QueueUser pivot : pivots) {
                List<QueueUser> finalTeam = strategy.buildAnyTeam(pivot, totalPool, queueInfo);
                if (finalTeam.size() >= MatchingConstants.ONLY_ANY_QUEUE_MIN_TEAM_SIZE) {
                    queueInfo.setGroupSize(String.valueOf(finalTeam.size()));
                    attemptMatch(queueKey, queueInfo, finalTeam);
                    return;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (IllegalMonitorStateException e) {
                // 락이 이미 만료된 상황. 
                log.error("매칭 엔진 로직이 제한 시간(10초)을 초과했습니다");
            }
        }

    }

    private void attemptMatch(String queueKey, QueueInfo queueInfo, List<QueueUser> team) {

        log.info("[매칭 시도] queue: {}, 필요 인원: {}", queueKey, queueInfo.getGroupSize());

        // 큐에서 제거 -> 이거 바꾸기, userqueueinfo 제외하고 삭제
        queueService.removeUsersFromQueueWithoutUserQueueInfos(team, queueKey);

        String matchId = createMatch(queueKey, queueInfo, team);

        // 큐에 남은 사람이 없다면 해당 큐를 제거, 만약에 매칭이 취소된다면 큐가 없다면 다시 만드는 로직이 필요
        // 
        queueService.cleanQueueIfEmpty(queueKey);

        matchTimeoutExecutor.schedule(() -> {
            try {
                processMatchTimeout(matchId);
            } catch (Exception e) {
                log.error("[매칭 타임아웃 스케줄링 오류] queueKey: {}, 오류: {}", queueKey, e);
            }
        }, MatchingConstants.MATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }


    private void processMatchTimeout(String matchId) {
        String statusKey = MatchingConstants.MATCH_STATUS_KEY + matchId;

        List<Object> result = matchStatusService.getMatchStatusAtomically(matchId);
        log.info("[타임아웃 로직 시작] result: {}", result);

        if(result == null) {
            // 찰나의 순간에 수락과 타임아웃이 경합된 경우 타임아웃처리 하지 않음.
            return;
        }

        MatchStatusInfo matchStatusInfo = matchStatusService.getMatchStatus(result);

        if (matchStatusInfo == null) {
            log.warn("[매칭 상태 없음] matchId: {}", matchId);
            return;
        }

        for (MatchParticipant participant : matchStatusInfo.participants()) {
            if (matchStatusInfo.isAcceptedUser(participant.userId())) {
                notificationService.sendDeclineMatch(participant.userId(), matchId,
                        MatchDeclineResponse.Status.CANCELLED);
                queueService.rejoinQueue(participant.userId(), matchStatusInfo.queueKey(),
                        participant.score());
            } else {
                notificationService.sendDeclineMatch(participant.userId(), matchId,
                        MatchDeclineResponse.Status.REJECTED);
            }
        }

        log.info("========================================");
        log.info("🎯 [매칭 타임아웃] 게임: {}", matchStatusInfo.queueKey());
        log.info("👥 팀원: {}", matchStatusInfo.participants().stream().map(MatchParticipant::nickname).toList());
        log.info("🆔 matchId: {}", matchId);
        log.info("========================================");

    }

    private String createMatch(String queueKey, QueueInfo queueInfo, List<QueueUser> team) {

        List<MatchParticipant> participants = team.stream().map(this::createParticipant).toList();

        // participants.forEach(p -> queueService.removeUserCompletely(p.userId())); // user queue
        // status 큐에서 제거

        String matchId = UUID.randomUUID().toString();

        MatchStatusInfo matchStatusInfo = MatchStatusInfo.builder().groupSize(queueInfo.getGroupSize())
                .acceptCount(0).participants(participants).queueKey(queueKey).build();
                
        matchStatusService.createMatchStatus(matchId, matchStatusInfo,
                MatchingConstants.MATCH_STATUS_EXPIRE_SECONDS);

        for (MatchParticipant participant : participants) {
            notificationService.sendMatchFound(participant.userId(), matchId);
        }

        log.info("========================================");
        log.info("🎯 [매칭 성공] 게임: {}", queueInfo.getGameName());
        log.info("👥 팀원: {}", participants.stream().map(MatchParticipant::nickname).toList());
        log.info("🆔 matchId: {}", matchId);
        log.info("========================================");

        return matchId;
    }

    private MatchParticipant createParticipant(QueueUser user) {
        String userId = user.getUserId();
        double score = user.getScore();
        String nickname = userInfoHelper.getNickname(userId);


        return MatchParticipant.builder().userId(userId).score(score).nickname(nickname).build();
    }

}

