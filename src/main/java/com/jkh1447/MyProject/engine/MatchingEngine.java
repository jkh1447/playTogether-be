package com.jkh1447.MyProject.engine;

import java.util.Set;
import java.util.UUID;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.jkh1447.MyProject.domain.auth.AuthConstants;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import com.jkh1447.MyProject.service.matching.MatchingNotificationService;
import java.util.Map;
import com.jkh1447.MyProject.service.users.UsersService;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingEngine {

    private final UsersService usersService;
    private final MatchingNotificationService notificationService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Scheduled(fixedDelay = 2000)
    public void processMatching() {

        Set<Object> activeQueues =
                redisTemplate.opsForSet().members(MatchingConstants.ACTIVE_QUEUES_KEY);

        if (activeQueues == null || activeQueues.isEmpty()) {
            return;
        }

        for (Object queueKeyObj : activeQueues) {
            String queueKey = (String) queueKeyObj;
            int groupSize = extractSizeFromKey(queueKey);

            Long currentSize = redisTemplate.opsForZSet().size(queueKey);

            if (currentSize != null && currentSize >= groupSize) {
                matchTeam(queueKey, groupSize);
            }


        }
    }

    private void matchTeam(String queueKey, int groupSize) {
        log.info("[Match Attempt] Queue: {}, Required GroupSize: {}", queueKey, groupSize);
        Set<ZSetOperations.TypedTuple<Object>> teamMembers =
                redisTemplate.opsForZSet().popMin(queueKey, groupSize);

        if (teamMembers != null && teamMembers.size() == groupSize) {
            List<String> userIds =
                    teamMembers.stream().map(tuple -> (String) tuple.getValue()).toList();

            // 현재는 알림이 없으므로 로그로 확인
            log.info("========================================");
            log.info("🎯 [매칭 성공!] 게임: {}", queueKey.split(":")[1]);
            log.info("👥 팀원 명단: {}", userIds);
            log.info("========================================");

            String matchId = UUID.randomUUID().toString();
            String statusKey = MatchingConstants.MATCH_STATUS_KEY + matchId;

            Map<String, Object> matchStatus = new HashMap<>();

            // 매칭 취소시 큐 복귀를 위한 스코어 저장
            List<String> userWithScores = teamMembers.stream()
                    .map(tuple -> {
                        String userId = (String)tuple.getValue();
                        double score = tuple.getScore();
                        String nickname;

                        if (userId != null && userId.startsWith(AuthConstants.GUEST_TOKEN_PREFIX)) {
                            String guestIdPart = userId.split("_")[1].substring(0, 4);
                            nickname = "게스트_" + guestIdPart;
                        } else {
                            nickname = usersService.getNickname(Long.parseLong(userId));
                        }

                        return userId + ":" + score + ":" + nickname;
                    }).toList();

            String participantsData = String.join(",", userWithScores); // "userId : score, ..."


            matchStatus.put(MatchingConstants.MATCH_GROUP_SIZE, groupSize);
            matchStatus.put(MatchingConstants.MATCH_ACCEPT_COUNT, 0);
            matchStatus.put(MatchingConstants.MATCH_PARTICIPANTS_DATA, participantsData);
            matchStatus.put(MatchingConstants.MATCH_QUEUE_KEY, queueKey); // 매칭 취소시 복귀를 위함

            log.info("[Redis Write] Key: {}, Data: {}", statusKey, matchStatus);

            redisTemplate.opsForHash().putAll(statusKey, matchStatus);

            redisTemplate.expire(statusKey, Duration.ofSeconds(15));
            log.info("[Redis Expire] Key: {} set for 15s", statusKey);

            for (String userId : userIds) {
                notificationService.sendMatchFound(userId, matchId);
            }

        }

        // 큐에 남은 사람이 없다면 해당 큐를 제거, 만약에 매칭이 취소된다면 큐가 없다면 다시 만드는 로직이 필요
        Long remaining = redisTemplate.opsForZSet().size(queueKey);
        if (remaining == null || remaining == 0) {
            redisTemplate.opsForSet().remove(MatchingConstants.ACTIVE_QUEUES_KEY, queueKey);
        }
    }

    private int extractSizeFromKey(String queueKey) {
        try {
            String[] parts = queueKey.split(":");
            String sizePart = parts[2];
            return Integer.parseInt(sizePart.split("=")[1]);
        } catch (Exception e) {
            log.error("Failed to extract size from key: {}", queueKey, e);
            return 999;
        }
    }
}
