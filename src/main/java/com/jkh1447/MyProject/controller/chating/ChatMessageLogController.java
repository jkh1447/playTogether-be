package com.jkh1447.MyProject.controller.chating;


import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.jkh1447.MyProject.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import com.jkh1447.MyProject.service.chating.ChatMessageLogService;
import com.jkh1447.MyProject.dto.chating.ChatMessageLogDto;
import com.jkh1447.MyProject.dto.chating.GetChatMessageLogsRequest;

@RestController
@RequestMapping("/api/chatMessageLog")
@RequiredArgsConstructor
public class ChatMessageLogController {

  private final ChatMessageLogService chatMessageLogService;
  
  @GetMapping
  public ResponseEntity<ApiResponse<?>> getChatMessageLogs(@ModelAttribute GetChatMessageLogsRequest request) {
    List<ChatMessageLogDto> messages = chatMessageLogService.getChatMessageLogs(request.roomId());
    return ResponseEntity.ok().body(ApiResponse.success(messages));
  }
}
