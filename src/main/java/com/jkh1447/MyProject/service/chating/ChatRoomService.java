package com.jkh1447.MyProject.service.chating;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.jkh1447.MyProject.dto.chating.ParticipantsDto;
import com.jkh1447.MyProject.dto.chating.ParticipantsDto.ParticipantInfo;
import com.jkh1447.MyProject.domain.chating.ChatingConstants;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final RedisTemplate<String, Object> redisTemplate;

    public ParticipantsDto getParticipants(String roomId) {

        String roomStatusKey = MatchingConstants.ROOM_PARTICIPANTS_KEY + roomId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(roomStatusKey);

        List<ParticipantInfo> participants = entries.entrySet().stream().map(
                entry -> new ParticipantInfo((String) entry.getKey(), (String) entry.getValue()))
                .collect(Collectors.toList());

        return ParticipantsDto.createParticipantDto(participants);
    }

    public void removeParticipant(String roomId, String userId) {
        String roomParticipantsKey = MatchingConstants.ROOM_PARTICIPANTS_KEY + roomId;
        Long success = redisTemplate.opsForHash().delete(roomParticipantsKey, userId);

        String roomStatusKey = MatchingConstants.ROOM_STATUS_KEY + roomId;
        redisTemplate.opsForHash().put(roomStatusKey, userId, ChatingConstants.CHAT_STATUS_EXITED);
        log.info("채팅방에서 제거: 유저 {}, 방 {}, success: {}", userId, roomId, success);
        if (success != null && success > 0) {
            Long currentCount = redisTemplate.opsForHash().increment(roomStatusKey, ChatingConstants.CHAT_PARTICIPANTS_COUNT, -1L);
            if (currentCount != null && currentCount <= 0) {
                redisTemplate.delete(roomStatusKey);
            }
        }
    }



    public boolean isUserCanJoin(String userId, String roomId) {
        String roomStatusKey = MatchingConstants.ROOM_STATUS_KEY + roomId;
        String status = (String) redisTemplate.opsForHash().get(roomStatusKey, userId);

        if (status == null || ChatingConstants.CHAT_STATUS_EXITED.equals(status)) {
            return false;
        }

        return true;
    }
}
