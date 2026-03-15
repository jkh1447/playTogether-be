package com.jkh1447.MyProject.service.feedback;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.jkh1447.MyProject.repository.feedback.feedbackRepository;
import java.util.Arrays;
import com.jkh1447.MyProject.dto.feedback.FeedbackDto;
import com.jkh1447.MyProject.dto.feedback.UpdateFeedbackStatusDto;
import com.jkh1447.MyProject.global.exception.TooManyRequestException;
import com.jkh1447.MyProject.domain.feedback.Feedback;
import com.jkh1447.MyProject.domain.feedback.FeedbackCategory;
import com.jkh1447.MyProject.domain.feedback.FeedbackConstants;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import java.time.Duration;
import com.jkh1447.MyProject.dto.feedback.FeedbackCategoryDto;
import com.jkh1447.MyProject.domain.feedback.FeedbackStatus;
import org.springframework.transaction.annotation.Transactional;

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

    if (feedbackDto.getCategory().equals("")) {
      throw new IllegalArgumentException(FeedbackConstants.CATEGORY_IS_EMPTY_MESSAGE);
    }

    if (feedbackDto.getTitle().length() > 50 || feedbackDto.getContent().length() > 2000) {
        throw new IllegalArgumentException(FeedbackConstants.TITLE_AND_CONTENT_LENGTH_LIMIT_MESSAGE);
    }


    Feedback feedback = Feedback.builder()
    .category(feedbackDto.getCategory())
    .title(feedbackDto.getTitle())
    .content(feedbackDto.getContent())
    .userId(feedbackDto.getUserId())
    .status(FeedbackStatus.PENDING)
    .build();
    feedbackRepository.save(feedback);
  }

  public List<FeedbackCategoryDto> getFeedbackCategories() {
    return Arrays.stream(FeedbackCategory.values())
        .map(category -> new FeedbackCategoryDto(category.getValue(), category.getLabel()))
        .toList();
  }

  public long getFeedbackLength() {
    long total = feedbackRepository.count();
    return total;
  }

  public Page<Feedback> getFeedbackList(FeedbackCategory category, FeedbackStatus status, Pageable pageable) {
    if (category != null && status != null) {
        return feedbackRepository.findByCategoryAndStatus(category, status, pageable);
    } else if (category != null) {
        return feedbackRepository.findByCategory(category, pageable);
    } else if (status != null) {
        return feedbackRepository.findByStatus(status, pageable);
    } else {
        return feedbackRepository.findAll(pageable); // 아무 필터도 없을 때
    }
  }

  @Transactional
  public void updateFeedbackStatus(UpdateFeedbackStatusDto updateFeedbackStatusDto) {
    Feedback feedback = feedbackRepository.findById(updateFeedbackStatusDto.id())
        .orElseThrow(() -> new IllegalArgumentException("해당 ID의 피드백을 찾을 수 없습니다."));

    feedback.setStatus(updateFeedbackStatusDto.status());

  }
}
