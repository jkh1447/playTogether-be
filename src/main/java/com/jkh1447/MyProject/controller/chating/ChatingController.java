package com.jkh1447.MyProject.controller.chating;

import org.springframework.stereotype.Controller;
import com.jkh1447.MyProject.dto.chating.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import com.jkh1447.MyProject.service.chating.ChatingService;
import com.jkh1447.MyProject.service.chating.ChatMessageSenderService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatingController {
    
    private final ChatingService chatingService;
    private final ChatMessageSenderService chatMessageSenderService;
    
    // app/chat/{roomId}로 메세지가 오면 실행됨.
    @MessageMapping("/chat/{roomId}")
    public void sendMessage(@DestinationVariable String roomId, @Payload ChatMessageDto chatMessage) {

        log.info("메세지 수신: roomId {}, chatMessage {}", roomId, chatMessage);
        
        ChatMessageDto message = chatingService.createChatMessageDto(roomId, chatMessage);
        
        chatMessageSenderService.sendChatMessage(roomId, message);
    }

    
}
