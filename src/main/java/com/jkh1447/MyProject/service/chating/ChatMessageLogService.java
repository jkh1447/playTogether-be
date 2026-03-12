package com.jkh1447.MyProject.service.chating;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.jkh1447.MyProject.repository.chating.ChatMessageRepository;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.jkh1447.MyProject.domain.chating.ChatMessage;
import com.jkh1447.MyProject.dto.chating.ChatMessageLogDto;

@Service
@RequiredArgsConstructor
public class ChatMessageLogService {
  
  private final ChatMessageRepository chatMessageRepository;

  public List<ChatMessageLogDto> getChatMessageLogs(String roomId) {
    List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByCreatedAtDesc(roomId);

    List<ChatMessageLogDto> messagesList = messages.stream().map(message -> 
                            ChatMessageLogDto.builder()
                            .id(String.valueOf(message.getId()))
                            .roomId(roomId)
                            .senderId(message.getSenderId())
                            .senderNickname(message.getSenderNickname())
                            .content(message.getContent())
                            .clientIp(message.getClientIp())
                            .userAgent(message.getUserAgent())
                            .createdAt(message.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                            .build())
                            .toList();
    return messagesList;
  }

}
