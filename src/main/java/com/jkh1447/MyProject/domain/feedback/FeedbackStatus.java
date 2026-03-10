package com.jkh1447.MyProject.domain.feedback;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FeedbackStatus {
  PENDING("pending", "대기"),
  IN_PROGRESS("in_progress", "진행중"),
  COMPLETED("completed", "완료");

  private final String value;
  private final String label;

  FeedbackStatus(String value, String label) {
    this.value = value;
    this.label = label;
  }

  @JsonValue // 객체 -> json 변환 시 사용, 서버 -> 클라이언트
  public String getValue() {
    return value;
  }

  @JsonCreator // json -> 객체 변환 시 사용, 클라이언트 -> 서버
  public static FeedbackStatus fromValue(String value) {
    for (FeedbackStatus status : FeedbackStatus.values()) {
      if (status.value.equals(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("존재하지 않는 상태입니다: " + value);
  }
}
