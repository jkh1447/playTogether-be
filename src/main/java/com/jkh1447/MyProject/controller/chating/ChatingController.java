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

        Object sessionUserId = accessor.getSessionAttributes().get("userId");
        Object sessionNickname = accessor.getSessionAttributes().get("nickname");

        if (sessionUserId == null || sessionNickname == null) {
            throw new MessageDeliveryException("세션 정보가 유효하지 않습니다. 다시 연결해주세요.");
        }

        String senderId = sessionUserId.toString();
        String senderNickname = sessionNickname.toString();

        if (chatMessage.content().length() > 1000) {
            throw new MessageDeliveryException("메시지 내용이 너무 깁니다.");
        }

        // // 클라이언트가 보낸 senderId/senderNickname은 신뢰하지 않고
        // // 서버 세션에서 검증된 값으로 메시지를 새로 구성 (위조 방지)
        if (!chatMessage.senderId().equals(senderId) || !chatMessage.senderNickname().equals(senderNickname)) {
            log.warn("[사칭 시도 감지] 세션유저: {}, 메시지발신자: {}", senderId, chatMessage.senderId());
            throw new MessageDeliveryException("발신자 정보가 일치하지 않습니다.");
        }

        // ChatMessageDto verifiedMessage =
        //         ChatMessageDto.builder().type(chatMessage.type()).roomId(roomId).senderId(senderId)
        //                 .senderNickname(senderNickname).content(chatMessage.content()).build();

        log.info("메세지 수신: roomId {}, type {}, sender {}", roomId, chatMessage.type(), senderId);

        ChatMessageDto message = chatingService.createChatMessageDto(roomId, chatMessage);

        chatMessageSenderService.sendChatMessage(roomId, message);
    }

    @MessageExceptionHandler
    @SendToUser("/queue/error")
    public String handleException(MessageDeliveryException e) {
        return e.getMessage();
    }



}
