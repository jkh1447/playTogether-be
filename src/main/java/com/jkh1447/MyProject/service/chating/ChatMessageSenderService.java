package com.jkh1447.MyProject.service.chating;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.jkh1447.MyProject.domain.chating.ChatingConstants;
import com.jkh1447.MyProject.dto.chating.ChatMessageDto;
import com.jkh1447.MyProject.dto.chating.ParticipantsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageSenderService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomService chatRoomService;

    // public ChatMessageSenderService(@Lazy SimpMessagingTemplate messagingTemplate,
    // ChatRoomService chatRoomService) {
    // this.messagingTemplate = messagingTemplate;
    // this.chatRoomService = chatRoomService;
    // }

    public void sendLeaveMessage(String roomId, String userId, String nickname) {
        ChatMessageDto leaveMessage = ChatMessageDto.createLeaveMessage(roomId, userId, nickname);
        log.info("[나가기 메세지 전송] 방 {}, 유저 {}, 메세지 {}", roomId, userId, leaveMessage);
        messagingTemplate.convertAndSend(ChatingConstants.SUB_ROOM_PATH + roomId, leaveMessage);
    }

    public void sendParticipants(String roomId) {
        ParticipantsDto participants = chatRoomService.getParticipants(roomId);
        messagingTemplate.convertAndSend(ChatingConstants.SUB_ROOM_PATH + roomId, participants);
    }

    public void sendChatMessage(String roomId, ChatMessageDto chatMessage) {
        messagingTemplate.convertAndSend(ChatingConstants.SUB_ROOM_PATH + roomId, chatMessage);
        log.info("채팅 메세지 전송: 방 {}, 메세지 {}", roomId, chatMessage);
    }
}
