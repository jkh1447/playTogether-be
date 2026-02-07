package com.jkh1447.MyProject.service.chating;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.jkh1447.MyProject.dto.chating.ParticipantDto;
import com.jkh1447.MyProject.dto.chating.ParticipantDto.ParticipantInfo;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    
    private final RedisTemplate<String, Object> redisTemplate;

    public ParticipantDto getParticipants(String roomId) {
        
        String roomStatusKey = MatchingConstants.ROOM_STATUS_KEY + roomId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(roomStatusKey);
        
        List<ParticipantInfo> participants = entries.entrySet().stream()
                .map(entry -> new ParticipantInfo((String) entry.getKey(), (String) entry.getValue()))
                .collect(Collectors.toList());
        
        return ParticipantDto.createParticipantDto(participants);
    }

    public void removeParticipant(String roomId, String userId) {
        String roomStatusKey = MatchingConstants.ROOM_STATUS_KEY + roomId;
        redisTemplate.opsForHash().delete(roomStatusKey, userId);
    }
}
