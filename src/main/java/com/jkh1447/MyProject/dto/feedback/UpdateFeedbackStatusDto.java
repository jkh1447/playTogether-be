package com.jkh1447.MyProject.dto.feedback;

import com.jkh1447.MyProject.domain.feedback.FeedbackStatus;
import lombok.Builder;

@Builder
public record UpdateFeedbackStatusDto(Long id, FeedbackStatus status) {
  
}
