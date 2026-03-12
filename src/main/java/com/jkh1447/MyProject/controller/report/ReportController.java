package com.jkh1447.MyProject.controller.report;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.jkh1447.MyProject.domain.report.ReportReason;
import com.jkh1447.MyProject.domain.report.ReportStatus;
import com.jkh1447.MyProject.dto.report.ReportDto;
import com.jkh1447.MyProject.service.report.ReportService;
import com.jkh1447.MyProject.global.response.ApiResponse;
import java.beans.PropertyEditorSupport;
import org.springframework.data.domain.Page;
import com.jkh1447.MyProject.domain.feedback.FeedbackCategory;
import com.jkh1447.MyProject.domain.feedback.FeedbackStatus;
import com.jkh1447.MyProject.domain.report.Report;
import com.jkh1447.MyProject.dto.gameInfo.UpdateReportStatusDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Slf4j
public class ReportController {
  
  private final ReportService reportService;

  @PostMapping("/{roomId}")
  public ResponseEntity<?> reportRoom(
    @PathVariable String roomId,
    @RequestBody ReportDto reportDto
  ) {
    reportService.reportRoom(reportDto, roomId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @GetMapping("/category")
  public ResponseEntity<?> getReportCategory() {
    return ResponseEntity.ok(ApiResponse.success(reportService.getReportCategory()));
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(ReportStatus.class, new PropertyEditorSupport() {
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            setValue(ReportStatus.fromValue(text));
        }
    });

    binder.registerCustomEditor(ReportReason.class, new PropertyEditorSupport() {
        @Override
        public void setAsText(String text) {
            setValue(ReportReason.fromValue(text));
        }
    });
  }

  @GetMapping
  public ResponseEntity<ApiResponse<?>> getReports(
    @RequestParam(required = false) ReportStatus status,
    @RequestParam(required = false) ReportReason reason,
    @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    Page<Report> reportPage = reportService.getReportList(reason, status, pageable);
    return ResponseEntity.ok(ApiResponse.success(reportPage));
  }

  @PatchMapping("/update/status")
  public ResponseEntity<?> updateReportStatus(@RequestBody UpdateReportStatusDto dto) {
    reportService.updateReportStatus(dto);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

}
