package com.jkh1447.MyProject.dto.report;

import com.jkh1447.MyProject.domain.report.ReportReason;
import lombok.Builder;

@Builder
public record ReportDto(
  String roomId,
  ReportReason reason,
  String detail
) {
}
