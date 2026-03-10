package com.jkh1447.MyProject.domain.feedback;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FeedbackCategory {
  BUG("bug", "버그"), ADD("add", "게임 추가 요청"), FEATURE("feature", "기능 요청"), IMPROVE("improve", "개선 사항"), OTHER("other", "기타");

  private final String value;
  private final String label;

  FeedbackCategory(String value, String label) {
    this.value = value;
    this.label = label;
  }

  @JsonValue // 서버 -> 클라이언트
  public String getValue() {
    return value;
  }

  public String getLabel() {
    return label;
  }

  @JsonCreator
  public static FeedbackCategory fromValue(String value) {
    for (FeedbackCategory category : FeedbackCategory.values()) {
      if (category.value.equals(value)) {
        return category;
      }
    }
    throw new IllegalArgumentException("비정상적인 입력입니다.");
  }
}
