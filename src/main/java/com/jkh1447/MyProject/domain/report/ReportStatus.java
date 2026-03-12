package com.jkh1447.MyProject.domain.report;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReportStatus {
  PENDING("pending", "대기"),
  IN_PROGRESS("in_progress", "진행중"),
  COMPLETED("completed", "완료");

  private final String value;
  private final String label;

  ReportStatus(String value, String label) {
    this.value = value;
    this.label = label;
  }

  @JsonValue // 객체 -> json 변환 시 사용, 서버 -> 클라이언트
  public String getValue() {
    return value;
  }

  @JsonCreator // json -> 객체 변환 시 사용, 클라이언트 -> 서버
  public static ReportStatus fromValue(String value) {
    for (ReportStatus status : ReportStatus.values()) {
      if (status.value.equals(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("존재하지 않는 상태입니다: " + value);
  }
}
