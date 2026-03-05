package com.jkh1447.MyProject.service.feedback;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.jkh1447.MyProject.repository.feedback.feedbackRepository;
import com.jkh1447.MyProject.dto.feedback.FeedbackDto;
import com.jkh1447.MyProject.global.exception.TooManyRequestException;
import com.jkh1447.MyProject.domain.feedback.Feedback;
import java.time.LocalDateTime;
import org.springframework.data.redis.core.RedisTemplate;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {
  
  private final feedbackRepository feedbackRepository;
  private final RedisTemplate<String, Object> redisTemplate;
 

  public void submitFeedback(FeedbackDto feedbackDto, String userId) {

    // 너무 많은 피드백 제출 제한
    String rateLimitKey = "feedback:count:" + userId;
    Long count = redisTemplate.opsForValue().increment(rateLimitKey);
    
    if (count == 1) {
        redisTemplate.expire(rateLimitKey, Duration.ofDays(1));
    }
    
    if (count > 5) {
        throw new TooManyRequestException();
    }

    if (feedbackDto.getTitle().length() > 50 || feedbackDto.getContent().length() > 2000) {
        throw new IllegalArgumentException("제목은 50자, 내용은 2000자 이내여야 합니다.");
    }


    Feedback feedback = Feedback.builder()
    .category(feedbackDto.getCategory())
    .title(feedbackDto.getTitle())
    .content(feedbackDto.getContent())
    .userId(feedbackDto.getUserId())
    .build();
    feedbackRepository.save(feedback);
  }
}
