package com.jkh1447.MyProject.service.matching;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import com.jkh1447.MyProject.dto.matching.MatchDeclineResponse;
import com.jkh1447.MyProject.dto.matching.MatchParticipant;
import com.jkh1447.MyProject.dto.matching.QueueInfo;
import com.jkh1447.MyProject.dto.matching.MatchStatusInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingEngineService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MatchingNotificationService notificationService;
    private final MatchQueueService queueService;
    private final UserInfoHelper userInfoHelper;
    private final MatchStatusService matchStatusService;
    private final ScheduledExecutorService matchTimeoutExecutor;

    public void processMatching() {
        Set<Object> activeQueues =
                redisTemplate.opsForSet().members(MatchingConstants.ACTIVE_QUEUES_KEY);

        if (activeQueues == null || activeQueues.isEmpty()) {
            return;
        }

        for (Object queueKeyObj : activeQueues) {
            String queueKey = (String) queueKeyObj;
            processQueue(queueKey);
        }
    }

    private void processQueue(String queueKey) { // 매칭
        try {
            QueueInfo queueInfo = QueueInfo.fromQueueKey(queueKey);
            Long currentQueueSize = queueService.getQueueSize(queueKey);

            if (currentQueueSize != null && currentQueueSize >= queueInfo.groupSize()) {
                attemptMatch(queueKey, queueInfo);
            }
        } catch (Exception e) {
            log.error("[매칭 처리 오류] queueKey: {}", queueKey, e);
        }
    }

    private void attemptMatch(String queueKey, QueueInfo queueInfo) {

        log.info("[매칭 시도] queue: {}, 필요 인원: {}", queueKey, queueInfo.groupSize());
        Set<ZSetOperations.TypedTuple<Object>> teamMembers = // 큐에서 빼기
                redisTemplate.opsForZSet().popMin(queueKey, queueInfo.groupSize());

        // 찰나에 사용자가 종료했을때
        if (teamMembers == null || teamMembers.size() < queueInfo.groupSize()) {
            // 구현해야 함
            return;
        }

        String matchId = createMatch(queueKey, queueInfo, teamMembers);

        // 큐에 남은 사람이 없다면 해당 큐를 제거, 만약에 매칭이 취소된다면 큐가 없다면 다시 만드는 로직이 필요
        queueService.cleanQueueIfEmpty(queueKey);

        matchTimeoutExecutor.schedule(() -> {
            try{
                processMatchTimeout(matchId);        
            } catch (Exception e) {
                log.error("[매칭 타임아웃 스케줄링 오류] queueKey: {}, 오류: {}", queueKey, e.getMessage());
            }
        }, MatchingConstants.MATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private void processMatchTimeout(String matchId){
        String statusKey = MatchingConstants.MATCH_STATUS_KEY + matchId;
        MatchStatusInfo matchStatusInfo = matchStatusService.getMatchStatus(matchId);
        
        if (Boolean.FALSE.equals(redisTemplate.delete(statusKey))) {
            // 이미 매칭이 처리된 경우 (거절 or 성사)
            // 동시성 방지
            return;
        }

        if (matchStatusInfo == null) {
            log.warn("[매칭 상태 없음] matchId: {}", matchId);
            return;
        }


        for(MatchParticipant participant : matchStatusInfo.participants()) {
            if(matchStatusInfo.isAcceptedUser(participant.userId())) {
                notificationService.sendDeclineMatch(participant.userId(), matchId,
                        MatchDeclineResponse.Status.CANCELLED);
                queueService.rejoinQueue(participant.userId(), matchStatusInfo.queueKey(), participant.score());
            }
            else {
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

    private String createMatch(String queueKey, QueueInfo queueInfo, Set<ZSetOperations.TypedTuple<Object>> teamMembers) {
        
        List<MatchParticipant> participants = teamMembers.stream().map(this::createParticipant).toList();

        participants.forEach(p -> queueService.removeUserCompletely(p.userId())); // user queue status���� ��嫄�

        String matchId = UUID.randomUUID().toString();
        
        MatchStatusInfo matchStatusInfo = MatchStatusInfo.builder()
                .groupSize(queueInfo.groupSize())
                .acceptCount(0)
                .participants(participants)
                .queueKey(queueKey)
                .build();
        matchStatusService.createMatchStatus(matchId, matchStatusInfo, MatchingConstants.MATCH_STATUS_EXPIRE_SECONDS);

        for (MatchParticipant participant : participants) {
            notificationService.sendMatchFound(participant.userId(), matchId);
        }

        log.info("========================================");
        log.info("🎯 [매칭 성공] 게임: {}", queueInfo.gameName());
        log.info("👥 팀원: {}", participants.stream().map(MatchParticipant::nickname).toList());
        log.info("🆔 matchId: {}", matchId);
        log.info("========================================");

        return matchId;
    }

    private MatchParticipant createParticipant(ZSetOperations.TypedTuple<Object> tuple) {
        String userId = (String) tuple.getValue();
        double score = tuple.getScore();
        String nickname = userInfoHelper.getNickname(userId);


        return MatchParticipant.builder()
                .userId(userId)
                .score(score)
                .nickname(nickname)
                .build();
    }
}
    
