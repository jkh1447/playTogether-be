package com.jkh1447.MyProject.dto.chating;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record ChatMessageDto(
    Long id,
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

    public static ChatMessageDto createEnterMessage(Long id, String roomId, String senderId, String senderNickname) {
        return ChatMessageDto.builder()
                .id(id)
                .type(MessageType.ENTER)
                .roomId(roomId)
                .senderId(senderId)
                .senderNickname(senderNickname)
                .content(senderNickname + "님이 입장했습니다.")
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public static ChatMessageDto createLeaveMessage(Long id, String roomId, String senderId, String senderNickname) {
        return ChatMessageDto.builder()
                .id(id)
                .type(MessageType.LEAVE)
                .roomId(roomId)
                .senderId(senderId)
                .senderNickname(senderNickname)
                .content(senderNickname + "님이 퇴장했습니다.")
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public static ChatMessageDto createTalkMessage(Long id, String roomId, String senderId, String senderNickname, String content) {
        return ChatMessageDto.builder()
                .id(id)
                .type(MessageType.TALK)
                .roomId(roomId)
                .senderId(senderId)
                .senderNickname(senderNickname)
                .content(content)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

}
