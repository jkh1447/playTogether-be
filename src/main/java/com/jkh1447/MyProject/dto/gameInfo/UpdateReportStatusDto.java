package com.jkh1447.MyProject.dto.gameInfo;

import com.jkh1447.MyProject.domain.report.ReportStatus;
import lombok.Builder;

@Builder
public record UpdateReportStatusDto(Long id, ReportStatus status) {
  
}
