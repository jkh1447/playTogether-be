package com.jkh1447.MyProject.service.chating;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.jkh1447.MyProject.domain.chating.ChatingConstants;
import com.jkh1447.MyProject.dto.chating.ChatMessageDto;
import com.jkh1447.MyProject.dto.chating.ParticipantsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;

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
        messagingTemplate.convertAndSend(ChatingConstants.SUB_ROOM_PATH + roomId, leaveMessage);
    }

    public void sendParticipants(String roomId) {
        ParticipantsDto participants = chatRoomService.getParticipants(roomId);
        messagingTemplate.convertAndSend(ChatingConstants.SUB_ROOM_PATH + roomId, participants);
    }

    public void sendChatMessage(String roomId, ChatMessageDto chatMessage) {
        messagingTemplate.convertAndSend(ChatingConstants.SUB_ROOM_PATH + roomId, chatMessage);
    }
}
