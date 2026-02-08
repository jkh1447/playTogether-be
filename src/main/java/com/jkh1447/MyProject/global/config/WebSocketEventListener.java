package com.jkh1447.MyProject.global.config;

import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import com.jkh1447.MyProject.dto.chating.ChatMessageDto;
import com.jkh1447.MyProject.service.chating.ChatRoomService;
import com.jkh1447.MyProject.service.chating.ChatMessageSenderService;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import com.jkh1447.MyProject.dto.chating.ParticipantsDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ChatRoomService chatRoomService;
    private final ChatMessageSenderService chatMessageSenderService;

    // 웹소켓 연결이 끊어졌을 때 실행(비정상 종료, 브라우저 종료)
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("연결 끊어짐: {}", headerAccessor.getSessionAttributes());
        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String nickname = (String) headerAccessor.getSessionAttributes().get("nickname");

        if (roomId != null && userId != null) {
            // 채팅방에서 제거
            chatRoomService.removeParticipant(roomId, userId);

            log.info("채팅방에서 제거: 유저 {}, 방 {}", userId, roomId);

            chatMessageSenderService.sendLeaveMessage(roomId, userId, nickname);

            chatMessageSenderService.sendParticipants(roomId);

            headerAccessor.getSessionAttributes().remove("userId");
            headerAccessor.getSessionAttributes().remove("roomId");
            headerAccessor.getSessionAttributes().remove("nickname");
        }

    }
}
