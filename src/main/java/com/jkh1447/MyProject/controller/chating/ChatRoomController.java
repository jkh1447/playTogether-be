package com.jkh1447.MyProject.controller.chating;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jkh1447.MyProject.dto.chating.ParticipantInfo;
import com.jkh1447.MyProject.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomController {
    
    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/room/{roomId}/participants")
    public ResponseEntity<ApiResponse<List<ParticipantInfo>>> getParticipants(@PathVariable String roomId) {
        String roomKey = "chat:room:" + roomId + ":participants";
        
        // Redis Hash에서 모든 필드와 값을 가져옴
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(roomKey);
        
        List<ParticipantInfo> participants = entries.entrySet().stream()
                .map(entry -> new ParticipantInfo((String) entry.getKey(), (String) entry.getValue()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(participants));
    }
}
