package com.jkh1447.MyProject.controller.chating;

import org.springframework.stereotype.Controller;
import com.jkh1447.MyProject.dto.chating.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import com.jkh1447.MyProject.service.chating.ChatingService;
import com.jkh1447.MyProject.service.chating.ChatMessageSenderService;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import com.jkh1447.MyProject.domain.chating.ChatingConstants;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatingController {
    
    private final ChatingService chatingService;
    private final ChatMessageSenderService chatMessageSenderService;
    
    // app/chat/{roomId}로 메세지가 오면 실행됨.
    @MessageMapping("/chat/{roomId}")
    public void sendMessage(@DestinationVariable String roomId, @Payload ChatMessageDto chatMessage,
        SimpMessageHeaderAccessor accessor) {
            
        log.info("메세지 수신: roomId {}, type {}, sender {}", roomId, chatMessage.type(), chatMessage.senderId());

        Object sessionUserId = accessor.getSessionAttributes().get("userId");
        Object sessionNickname = accessor.getSessionAttributes().get("nickname");
        Object sessionClientIp = accessor.getSessionAttributes().get("clientIp");
        Object sessionUserAgent = accessor.getSessionAttributes().get("userAgent");

        if (sessionUserId == null || sessionNickname == null) {
            throw new MessageDeliveryException(ChatingConstants.INVALID_SESSION_INFO);
        }

        String senderId = sessionUserId.toString();
        String senderNickname = sessionNickname.toString();

        if (chatMessage.content().length() > 1000) {
            throw new MessageDeliveryException(ChatingConstants.MESSAGE_TOO_LONG);
        }

        // // 클라이언트가 보낸 senderId/senderNickname은 신뢰하지 않고
        // // 서버 세션에서 검증된 값으로 메시지를 새로 구성 (위조 방지)
        if (!chatMessage.senderId().equals(senderId) || !chatMessage.senderNickname().equals(senderNickname)) {
            log.warn("[사칭 시도 감지] 세션유저: {}, 메시지발신자: {}", senderId, chatMessage.senderId());
            throw new MessageDeliveryException(ChatingConstants.SENDER_INFO_MISMATCH);
        }

        if (sessionClientIp == null || sessionUserAgent == null) {
            throw new MessageDeliveryException(ChatingConstants.SESSION_EXPIRED);
        }

        chatingService.saveChatMessage(chatMessage, sessionClientIp.toString(), sessionUserAgent.toString());

        ChatMessageDto message = chatingService.createChatMessageDto(roomId, chatMessage);

        chatMessageSenderService.sendChatMessage(roomId, message);
    }

    @MessageExceptionHandler
    @SendToUser("/queue/error")
    public String handleException(MessageDeliveryException e) {
        return e.getMessage();
    }

    @MessageMapping("/chat/{roomId}/check")
    public void checkRoomSubscription(@DestinationVariable String roomId, SimpMessageHeaderAccessor accessor) {
        
    }


}
