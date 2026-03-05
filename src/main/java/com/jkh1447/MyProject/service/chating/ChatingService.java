package com.jkh1447.MyProject.service.chating;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
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

        String safeNickname = HtmlUtils.htmlEscape(chatMessage.senderNickname());
        String senderId = chatMessage.senderId();


        switch(chatMessage.type()) {
            case ENTER:
                message = ChatMessageDto.createEnterMessage(roomId, senderId, safeNickname);
                break;
            case TALK:
                String rawContent = chatMessage.content();
                if (rawContent == null || rawContent.trim().isEmpty()) {
                    // 예외 바꾸기
                    throw new IllegalArgumentException("메시지 내용이 비어있습니다.");
                }

                if (rawContent.length() > 1000) {
                    throw new IllegalArgumentException("메시지는 1000자 이하로 입력해주세요.");
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
}
