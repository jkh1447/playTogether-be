package com.jkh1447.MyProject.dto.chating;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
public record ChatMessageDto(
    MessageType type,
    String roomId,
    String senderId,
    String senderNickname,
    String content,
    String timestamp) {
    
    public enum MessageType {
        ENTER,
        TALK,
        LEAVE
    }

    public static ChatMessageDto createEnterMessage(String roomId, String senderId, String senderNickname) {
        return ChatMessageDto.builder()
                .type(MessageType.ENTER)
                .roomId(roomId)
                .senderId(senderId)
                .senderNickname(senderNickname)
                .content(senderNickname + "님이 입장했습니다.")
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public static ChatMessageDto createLeaveMessage(String roomId, String senderId, String senderNickname) {
        return ChatMessageDto.builder()
                .type(MessageType.LEAVE)
                .roomId(roomId)
                .senderId(senderId)
                .senderNickname(senderNickname)
                .content(senderNickname + "님이 퇴장했습니다.")
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public static ChatMessageDto createTalkMessage(String roomId, String senderId, String senderNickname, String content) {
        return ChatMessageDto.builder()
                .type(MessageType.TALK)
                .roomId(roomId)
                .senderId(senderId)
                .senderNickname(senderNickname)
                .content(content)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}
