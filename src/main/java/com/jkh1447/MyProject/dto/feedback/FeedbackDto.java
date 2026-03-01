package com.jkh1447.MyProject.dto.feedback;

import lombok.Builder;
import lombok.Setter;
import lombok.Getter;

@Builder
@Setter
@Getter
public class FeedbackDto {
  private String category;
  private String title;
  private String content;
  private String userId;


}
