package com.jkh1447.MyProject.controller.report;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jkh1447.MyProject.dto.report.ReportDto;
import com.jkh1447.MyProject.service.report.ReportService;
import com.jkh1447.MyProject.global.response.ApiResponse;

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
}
