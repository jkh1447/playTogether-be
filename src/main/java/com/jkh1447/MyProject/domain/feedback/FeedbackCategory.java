package com.jkh1447.MyProject.domain.feedback;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FeedbackCategory {
  BUG("bug"), ADD("add"), FEATURE("feature"), IMPROVE("improve"), OTHER("other");

  private final String value;

  FeedbackCategory(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
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
