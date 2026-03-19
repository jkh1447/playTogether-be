package com.jkh1447.MyProject.global.config.webSocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.jkh1447.MyProject.service.chating.ChatRoomService;
import com.jkh1447.MyProject.domain.chating.ChatingConstants;
import com.jkh1447.MyProject.dto.chating.ParticipantsDto;
import com.jkh1447.MyProject.service.chating.ChatMessageSenderService;
import com.jkh1447.MyProject.service.matching.RoomService;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private final ChatRoomService chatRoomService;
    private final ChatMessageSenderService chatMessageSenderService;
    private final RoomService roomService;

    private final String CHAT_SUB_TYPE = "CHAT";
    private final String SUB_TYPE = "SUB_TYPE_";
    

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

        // 소켓 연결 메세지인 경우
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

         

            String userId = accessor.getSessionAttributes().get("userId").toString();
            String nickname = accessor.getSessionAttributes().get("nickname").toString();
            

            log.info("소켓 연결: {}, {}", userId, nickname);
        }
        // 구독 메세지인 경우
        else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) && destination != null
                && destination.startsWith(ChatingConstants.SUB_ROOM_PATH)) {
            try {
                handleSubscribe(accessor);
            } catch (MessageDeliveryException e) {
                log.error("구독 실패: {}", e.getMessage());
                return null;
            }
        } else if (StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {
            handleUnsubscribe(accessor);
        }
        return message;
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {

        // 방 구독 관련 정보 저장
        String roomId = accessor.getFirstNativeHeader("roomId");
        String destination = accessor.getDestination();
        String subscriptionId = accessor.getSubscriptionId();

        if (destination != null && destination.startsWith(ChatingConstants.SUB_ROOM_PATH)) {
            accessor.getSessionAttributes().put(SUB_TYPE + subscriptionId, CHAT_SUB_TYPE);

            log.info("구독 정보 저장 완료: 방 {}, 구독 ID {}", roomId, subscriptionId);

            if (accessor.getSessionAttributes() == null) {
                throw new MessageDeliveryException("세션 정보를 불러오는 도중 오류가 발생했습니다.");
            }

            if (accessor.getSessionAttributes().get("nickname") == null) {
                throw new MessageDeliveryException("세션 정보의 닉네임을 불러오는 도중 오류가 발생했습니다.");
            }

            if (accessor.getSessionAttributes().get("userId") == null) {
                throw new MessageDeliveryException("세션 정보의 유저아이디를 불러오는 도중 오류가 발생했습니다.");
            }

            String nickname = accessor.getSessionAttributes().get("nickname").toString();
            String userId = accessor.getSessionAttributes().get("userId").toString();

            if (!chatRoomService.isUserCanJoin(userId, roomId)) {
                throw new MessageDeliveryException("권한이 없는 채팅방입니다.");
            }
            // 구독했을 때 참가자에 추가
            roomService.addParticipant(roomId, userId, nickname);
            chatMessageSenderService.sendParticipants(roomId);

            ParticipantsDto participants = chatRoomService.getParticipants(roomId);
            chatMessageSenderService.sendParticipantsToUser(userId, participants);
            
    
            if (accessor.getSessionAttributes() != null) {
                accessor.getSessionAttributes().put("roomId", roomId);
                accessor.getSessionAttributes().put("nickname", nickname);
                accessor.getSessionAttributes().put("canChat", true);
                log.info("세션 정보 저장 완료: 방 {}", roomId);
            }
        } 

       


    }



    private void handleUnsubscribe(StompHeaderAccessor accessor) {

        String subscriptionId = accessor.getSubscriptionId();
        String subType = (String) accessor.getSessionAttributes().get(SUB_TYPE + subscriptionId);

        if (subType != null && CHAT_SUB_TYPE.equals(subType)) {
            
            String roomId = (String) accessor.getSessionAttributes().get("roomId");
            String userId = (String) accessor.getSessionAttributes().get("userId");
            String nickname = (String) accessor.getSessionAttributes().get("nickname");
    
            if (roomId != null && userId != null) {
                // 채팅방에서 제거
                chatRoomService.removeParticipant(roomId, userId);
    
                chatMessageSenderService.sendLeaveMessage(roomId, userId, nickname);
    
                chatMessageSenderService.sendParticipants(roomId);
    
                // roomId만 제거: WebSocket 연결이 유지되므로 userId는 남겨둠
                // (이후 브라우저 종료 시 WebSocketEventListener에서 userId 사용)
                // roomId 제거로 DISCONNECT 이벤트의 채팅방 처리 중복 방지
                accessor.getSessionAttributes().remove("roomId");
                accessor.getSessionAttributes().remove("canChat");
                log.info("[채팅방 퇴장] userId: {}, roomId: {}", userId, roomId);
            }
        }


    }
}
