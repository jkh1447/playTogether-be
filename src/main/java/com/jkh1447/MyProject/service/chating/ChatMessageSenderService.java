package com.jkh1447.MyProject.service.chating;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.jkh1447.MyProject.dto.chating.ChatMessageDto;
import com.jkh1447.MyProject.dto.chating.ParticipantDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageSenderService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomService chatRoomService;

    public void sendLeaveMessage(String roomId, String userId, String nickname) {
        ChatMessageDto leaveMessage = ChatMessageDto.createLeaveMessage(roomId, userId, nickname);
        messagingTemplate.convertAndSend("/topic/room/" + roomId, leaveMessage);
    }

    public void sendParticipants(String roomId) {
        ParticipantDto participants = chatRoomService.getParticipants(roomId);
        messagingTemplate.convertAndSend("/topic/room/" + roomId, participants);
    }
}
