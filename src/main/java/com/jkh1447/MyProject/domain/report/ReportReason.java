package com.jkh1447.MyProject.domain.report;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReportReason {
  ABUSE("abuse"),
  SPAM("spam"),
  OTHER("other");

  private final String description;

  ReportReason(String description) {
    this.description = description;
  }

  @JsonValue // 객체 -> json 변환 시 사용
  public String getDescription() {
    return description;
  }

  @JsonCreator // json -> 객체 변환 시 사용
  public static ReportReason fromDescription(String description) {
    for (ReportReason reason : ReportReason.values()) {
      if (reason.description.equals(description)) {
        return reason;
      }
    }
    throw new IllegalArgumentException("비정상적인 입력입니다.");
  }
}