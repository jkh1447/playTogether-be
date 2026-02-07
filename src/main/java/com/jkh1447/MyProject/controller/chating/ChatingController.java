package com.jkh1447.MyProject.controller.chating;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.jkh1447.MyProject.dto.chating.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import com.jkh1447.MyProject.service.chating.ChatingService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatingController {
    
    private final ChatingService chatingService;
    private final SimpMessagingTemplate messagingTemplate;
    
    // app/chat.sendMessage/{roomId}로 메세지가 오면 실행됨.
    @MessageMapping("/chat.sendMessage/{roomId}")
    public void sendMessage(@DestinationVariable String roomId, @Payload ChatMessageDto chatMessage) {
        
        ChatMessageDto message = chatingService.sendMessage(roomId, chatMessage);
        
        // 해당 주소는 프론트엔드에서 구독해야함.
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, message);
    }
}
