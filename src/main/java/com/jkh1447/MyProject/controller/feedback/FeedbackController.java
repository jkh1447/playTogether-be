package com.jkh1447.MyProject.controller.feedback;

import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.jkh1447.MyProject.dto.feedback.FeedbackDto;
import com.jkh1447.MyProject.service.feedback.FeedbackService;
import com.jkh1447.MyProject.security.JwtUtil;

@RestController
@RequestMapping("/api/feedback")
@Slf4j
@RequiredArgsConstructor
public class FeedbackController {
  
  private final FeedbackService feedbackService;

  @PostMapping("/submit")
  public void submitFeedback(@RequestBody FeedbackDto feedbackDto) {
    String userId = JwtUtil.getCurrentUserId();
    feedbackDto.setUserId(userId);
    feedbackService.submitFeedback(feedbackDto, userId);
    log.info("피드백 제출 성공: {}", feedbackDto);
  }
}
