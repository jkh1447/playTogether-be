package com.jkh1447.MyProject.repository.report;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jkh1447.MyProject.domain.report.Report;
import java.time.LocalDateTime;

public interface ReportRepository extends JpaRepository<Report, Long> {
  boolean existsByReporterIdAndRoomIdAndCreatedAtAfter(String reporterId, String roomId, LocalDateTime createdAt);
}