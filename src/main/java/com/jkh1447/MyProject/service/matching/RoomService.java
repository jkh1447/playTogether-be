package com.jkh1447.MyProject.service.matching;

import org.springframework.stereotype.Service;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import com.jkh1447.MyProject.dto.matching.MatchParticipant;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.List;
import java.util.Map;
import java.time.Duration;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    /* 
     * Redis Hash 구조
     * 1. Room Status // 방에 누가 있는가?
     * Key: room:status:{roomId}
     * Value: {userId:nickname, userId:nickname, ...}
     */
    
    private final RedisTemplate<String, Object> redisTemplate;

    /*
     * roomStatus 생성
     * 참가자들에게 방 이동 알림
     */

    public void createRoom(String roomId, List<MatchParticipant> participants) {
        String roomStatusKey = buildRoomStatusKey(roomId);

        Map<String, Object> roomStatus = new HashMap<>();
        for(MatchParticipant participant: participants) {
            roomStatus.put(participant.userId(), participant.nickname());
        }

        redisTemplate.opsForHash().putAll(roomStatusKey, roomStatus);
        redisTemplate.expire(roomStatusKey, Duration.ofSeconds(MatchingConstants.ROOM_EXPIRE_SECONDS));
    }

    private String buildRoomStatusKey(String roomId) {
        return MatchingConstants.ROOM_STATUS_KEY + roomId;
    }

    public Map<Object, Object> getRoomStatus(String roomId) {
        String roomStatusKey = buildRoomStatusKey(roomId);
        return redisTemplate.opsForHash().entries(roomStatusKey);
    }

    public void deleteRoom(String roomId) {
        String roomStatusKey = buildRoomStatusKey(roomId);
        Boolean isDeleted = redisTemplate.delete(roomStatusKey);

        if (Boolean.TRUE.equals(isDeleted)) {
            log.info("[방 삭제] roomId: {}", roomId);
        }
    }
}
