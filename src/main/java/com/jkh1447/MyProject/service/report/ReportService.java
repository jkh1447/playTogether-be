package com.jkh1447.MyProject.service.report;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.jkh1447.MyProject.dto.report.ReportDto;
import com.jkh1447.MyProject.domain.report.Report;
import com.jkh1447.MyProject.repository.report.ReportRepository;
import com.jkh1447.MyProject.security.JwtUtil;
import com.jkh1447.MyProject.domain.auth.exception.UserNotFoundException;
import java.time.LocalDateTime;
import com.jkh1447.MyProject.global.exception.TooManyRequestException;
import com.jkh1447.MyProject.repository.room.RoomRepository;
import com.jkh1447.MyProject.domain.report.ReportConstants;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;
import com.jkh1447.MyProject.dto.gameInfo.UpdateReportStatusDto;
import com.jkh1447.MyProject.dto.report.ReportCategoryDto;
import com.jkh1447.MyProject.domain.report.ReportReason;
import com.jkh1447.MyProject.domain.report.ReportStatus;
import org.springframework.transaction.annotation.Transactional;
import com.jkh1447.MyProject.repository.chating.ChatMessageRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

  private final ReportRepository reportRepository;
  private final RoomRepository roomRepository;
  private final ChatMessageRepository chatMessageRepository;

  @Transactional
  public void reportRoom(ReportDto reportDto, String roomId) {

    String currentUserId = JwtUtil.getCurrentUserId();
    if (currentUserId == null) {
      throw new UserNotFoundException();
    }

    Boolean isRoomExist = roomRepository.existsByRoomId(roomId);
    if (!isRoomExist) {
      throw new IllegalArgumentException(ReportConstants.ROOM_NOT_FOUND_MESSAGE);
    }

    if (reportDto.reason() == null || reportDto.reason().equals("") || reportDto.detail() == null
        || reportDto.detail().equals("")) {
      throw new IllegalArgumentException(ReportConstants.REASON_AND_DETAIL_EMPTY_MESSAGE);
    }

    if (reportDto.detail().length() > 1000) {
      throw new IllegalArgumentException(ReportConstants.REASON_AND_DETAIL_LENGTH_LIMIT_MESSAGE);
    }

    if (reportRepository.existsByReporterIdAndRoomIdAndCreatedAtAfter(currentUserId, roomId,
        LocalDateTime.now().minusDays(5))) {
      throw new TooManyRequestException();
    }

    Report report = Report.builder().roomId(roomId).reporterId(currentUserId)
        .reason(reportDto.reason()).detail(reportDto.detail()).status(ReportStatus.PENDING).build();

    chatMessageRepository.updateIsPreservedByRoomId(roomId);

    reportRepository.save(report);
  }

  public List<ReportCategoryDto> getReportCategory() {
    return Arrays.stream(ReportReason.values())
        .map(reason -> new ReportCategoryDto(reason.getValue(), reason.getLabel()))
        .collect(Collectors.toList());
  }

  public Page<Report> getReportList(ReportReason reason, ReportStatus status, Pageable pageable) {
    if (reason != null && status != null) {
        return reportRepository.findByReasonAndStatus(reason, status, pageable);
    } else if (reason != null) {
        return reportRepository.findByReason(reason, pageable);
    } else if (status != null) {
        return reportRepository.findByStatus(status, pageable);
    } else {
        return reportRepository.findAll(pageable); // 아무 필터도 없을 때
    }
  }

  @Transactional
  public void updateReportStatus(UpdateReportStatusDto dto) {
    Report report = reportRepository.findById(dto.id()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신고입니다."));
    report.setStatus(dto.status());

  }
}
