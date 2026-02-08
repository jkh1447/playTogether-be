package com.jkh1447.MyProject.service.chating;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import com.jkh1447.MyProject.dto.chating.ChatMessageDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatingService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ChatRoomService chatRoomService;
    private final ChatMessageSenderService chatMessageSenderService;

    public ChatMessageDto createChatMessageDto(String roomId, ChatMessageDto chatMessage) {
        
        String roomKey = MatchingConstants.ROOM_STATUS_KEY + roomId;

        ChatMessageDto message = null;

        switch(chatMessage.type()) {
            case ENTER:
                message = ChatMessageDto.createEnterMessage(roomId, chatMessage.senderId(), chatMessage.senderNickname());
                break;
            case TALK:
                message = ChatMessageDto.createTalkMessage(roomId, chatMessage.senderId(), chatMessage.senderNickname(), chatMessage.content());
                break;
            case LEAVE:
                message = ChatMessageDto.createLeaveMessage(roomId, chatMessage.senderId(), chatMessage.senderNickname());
                chatRoomService.removeParticipant(roomId, chatMessage.senderId());
                chatMessageSenderService.sendParticipants(roomId);
                break;
        }        

        return message;
    }

    // 참가자 나가기는 인터셉터에서 처리
    private void checkAndCleanupRoom(String roomKey) {
        Long size = redisTemplate.opsForHash().size(roomKey);
        if (size == null || size == 0) {
            redisTemplate.delete(roomKey);
            log.info("방이 비어 있어 삭제 처리되었습니다: {}", roomKey);
        }
    }
}
