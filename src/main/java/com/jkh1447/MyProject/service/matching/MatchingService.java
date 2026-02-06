package com.jkh1447.MyProject.service.matching;

import java.util.List;
import java.util.UUID;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.jkh1447.MyProject.dto.matching.MatchingRequest;
import java.time.Duration;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MatchingKeyGenerator keyGenerator;
    private final MatchingNotificationService notificationService;

    public void joinQueue(String userId, MatchingRequest request) {
        String queueKey = keyGenerator.generateKey(request); // match:gameName:groupSize=size:filterPart
        double score = System.currentTimeMillis();

        redisTemplate.opsForZSet().add(queueKey, userId, score);

        // 활성화 중인 큐. 이 목록만 엔진이 순회하면 된다.
        redisTemplate.opsForSet().add(MatchingConstants.ACTIVE_QUEUES_KEY, queueKey);
    }

    public void leaveQueue(String userId, MatchingRequest request) {
        String queueKey = keyGenerator.generateKey(request);

        redisTemplate.opsForZSet().remove(queueKey, userId);

        Long size = redisTemplate.opsForZSet().size(queueKey);
        if (size == null || size == 0) {
            redisTemplate.opsForSet().remove(MatchingConstants.ACTIVE_QUEUES_KEY, queueKey);
        }
    }

    public void acceptMatch(String userId, String matchId) {

        String statusKey = MatchingConstants.MATCH_STATUS_KEY + matchId;

        // 수락 버튼 누른 유저가 없을 경우에만 넣고 true 반환, 이미 있으면 false 반환
        Boolean isNewAccept =
                redisTemplate.opsForHash().putIfAbsent(statusKey, "user:" + userId, "ACCEPTED");

        if (Boolean.FALSE.equals(isNewAccept)) {
            return; // 수락버튼 광클 방지
        }

        Long currentCount = redisTemplate.opsForHash().increment(statusKey,
                MatchingConstants.MATCH_ACCEPT_COUNT, 1);

        Object rawGroupSize = redisTemplate.opsForHash().get(statusKey,
                MatchingConstants.MATCH_GROUP_SIZE);

        if (rawGroupSize == null) {
            // 잡혔던 큐가 취소되고 다시 대기열로
            log.warn("⚠️ 만료되었거나 존재하지 않는 매칭입니다: {}", matchId);
            return;
        }

        int groupSize = Integer.parseInt(rawGroupSize.toString());

        if (currentCount.equals(Long.valueOf(groupSize))) {
            log.info("✅ 매칭 최종 성사! matchId: {}", matchId);
            // 여기서 실제 방 생성(Room Create) 로직 호출

            String roomId = UUID.randomUUID().toString();

            String participantsStr = (String) redisTemplate.opsForHash().get(statusKey,
                    MatchingConstants.MATCH_PARTICIPANTS_DATA);
            List<String> userIds = Arrays.stream(participantsStr.split(",")).map(String::trim)
                    .filter(s -> !s.isEmpty()).map(data -> data.split(":")[0]).toList();

            for (String participants : userIds) {
                notificationService.sendMoveToRoom(participants, roomId);
            }

            addRoomStatusFromMatchStatus(matchId, roomId); // 방 생성에 필요한 정보들

            redisTemplate.delete(statusKey);
        }

    }

    private void addRoomStatusFromMatchStatus(String matchId, String roomId) {
        
        String roomStatusKey = MatchingConstants.ROOM_STATUS_KEY + roomId;

        String participantsData = (String) redisTemplate.opsForHash().get(MatchingConstants.MATCH_STATUS_KEY + matchId,
                MatchingConstants.MATCH_PARTICIPANTS_DATA);

        Map<String, String> roomStatus = new HashMap<>();

        for(String entry: participantsData.split(",")) {
            String[] parts = entry.split(":");
            String userId = parts[0];
            String nickname = parts[2];
            roomStatus.put(userId, nickname);
        }

        redisTemplate.opsForHash().putAll(roomStatusKey, roomStatus);
        redisTemplate.expire(roomStatusKey, Duration.ofHours(1)); // 방 유지시간
    }
}
