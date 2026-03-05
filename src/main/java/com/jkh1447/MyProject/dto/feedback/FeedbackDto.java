package com.jkh1447.MyProject.dto.feedback;

import com.jkh1447.MyProject.domain.feedback.FeedbackCategory;
import lombok.Builder;
import lombok.Setter;
import lombok.Getter;

@Builder
@Setter
@Getter
public class FeedbackDto {
  private FeedbackCategory category;
  private String title;
  private String content;
  private String userId;

}
