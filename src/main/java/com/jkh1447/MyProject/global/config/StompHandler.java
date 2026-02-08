package com.jkh1447.MyProject.global.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.jkh1447.MyProject.service.chating.ChatRoomService;
import com.jkh1447.MyProject.domain.chating.ChatingConstants;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import com.jkh1447.MyProject.service.chating.ChatMessageSenderService;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private final ChatRoomService chatRoomService;
    private final ChatMessageSenderService chatMessageSenderService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // 메세지 헤더 접근자
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // log.info("메세지 헤더: {}", accessor);

        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }
        String destination = accessor.getDestination();
        // 구독 메세지인 경우
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())
                && destination.startsWith(ChatingConstants.SUB_ROOM_PATH)) {
            handleSubscribe(accessor);
        } else if (StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {
            // handleUnsubscribe(accessor);
        }
        return message;
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String userId = accessor.getFirstNativeHeader("userId");
        String roomId = accessor.getFirstNativeHeader("roomId");
        String nickname = accessor.getFirstNativeHeader("nickname");

        if (accessor.getSessionAttributes() != null) {
            accessor.getSessionAttributes().put("userId", userId);
            accessor.getSessionAttributes().put("roomId", roomId);
            accessor.getSessionAttributes().put("nickname", nickname);
            log.info("세션 정보 저장 완료: 유저 {}, 방 {}", userId, roomId);
        }
    }

    private void handleUnsubscribe(StompHeaderAccessor accessor) {
        String roomId = (String) accessor.getSessionAttributes().get("roomId");
        String userId = (String) accessor.getSessionAttributes().get("userId");
        String nickname = (String) accessor.getSessionAttributes().get("nickname");

        if (roomId != null && userId != null) {
            // 채팅방에서 제거
            chatRoomService.removeParticipant(roomId, userId);

            chatMessageSenderService.sendLeaveMessage(roomId, userId, nickname);

            chatMessageSenderService.sendParticipants(roomId);

            // 중복 삭제 방지
            accessor.getSessionAttributes().remove("userId");
            accessor.getSessionAttributes().remove("roomId");
            accessor.getSessionAttributes().remove("nickname");
        }
    }
}
