package com.jkh1447.MyProject.service.chating;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import com.jkh1447.MyProject.dto.chating.ChatMessageDto;
import com.jkh1447.MyProject.repository.chating.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.jkh1447.MyProject.domain.chating.ChatMessage;
import com.jkh1447.MyProject.domain.chating.ChatingConstants;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatingService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ChatRoomService chatRoomService;
    private final ChatMessageSenderService chatMessageSenderService;
    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageDto createChatMessageDto(String roomId, ChatMessageDto chatMessage) {
        
        String roomKey = MatchingConstants.ROOM_STATUS_KEY + roomId;
        ChatMessageDto message = null;

        String safeNickname = HtmlUtils.htmlEscape(chatMessage.senderNickname());
        String senderId = chatMessage.senderId();


        switch(chatMessage.type()) {
            case ENTER:
                message = ChatMessageDto.createEnterMessage(roomId, senderId, safeNickname);
                break;
            case TALK:
                String rawContent = chatMessage.content();
                if (rawContent == null || rawContent.trim().isEmpty()) {
                    throw new IllegalArgumentException(ChatingConstants.EMPTY_MESSAGE);
                }

                if (rawContent.length() > ChatingConstants.MESSAGE_LENGTH_LIMIT) {
                    throw new IllegalArgumentException(ChatingConstants.MESSAGE_LENGTH_LIMIT_MESSAGE);
                }

                String safeContent = HtmlUtils.htmlEscape(rawContent);
                message = ChatMessageDto.createTalkMessage(roomId, senderId, safeNickname, safeContent);
                break;
            case LEAVE:
                message = ChatMessageDto.createLeaveMessage(roomId, senderId, safeNickname);
                chatRoomService.removeParticipant(roomId, senderId);
                chatMessageSenderService.sendParticipants(roomId);
                break;
        }        

        return message;
    }

    // 참가자 나가기는 인터셉터에서 처리
    // private void checkAndCleanupRoom(String roomKey) {
    //     Long size = redisTemplate.opsForHash().size(roomKey);
    //     if (size == null || size == 0) {
    //         redisTemplate.delete(roomKey);
    //         log.info("방이 비어 있어 삭제 처리되었습니다: {}", roomKey);
    //     }
    // }
    
    public void saveChatMessage(ChatMessageDto message, String clientIp, String userAgent) {
        ChatMessage chatMessage = ChatMessage.builder()
            .roomId(message.roomId())
            .senderId(message.senderId())
            .senderNickname(message.senderNickname())
            .content(message.content())
            .clientIp(clientIp)
            .userAgent(userAgent)
            .build();
        chatMessageRepository.save(chatMessage);
    }
}
