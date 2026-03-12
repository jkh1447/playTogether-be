package com.jkh1447.MyProject.repository.report;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.jkh1447.MyProject.domain.report.Report;
import com.jkh1447.MyProject.domain.report.ReportReason;
import com.jkh1447.MyProject.domain.report.ReportStatus;
import java.time.LocalDateTime;

public interface ReportRepository extends JpaRepository<Report, Long> {
  boolean existsByReporterIdAndRoomIdAndCreatedAtAfter(String reporterId, String roomId, LocalDateTime createdAt);

  Page<Report> findByReasonAndStatus(ReportReason reason, ReportStatus status, Pageable pageable);
    
  // 필터 조건이 없거나 카테고리만 있는 경우 등을 위해 메서드 오버로딩
  Page<Report> findByReason(ReportReason category, Pageable pageable);
  Page<Report> findByStatus(ReportStatus status, Pageable pageable);
}