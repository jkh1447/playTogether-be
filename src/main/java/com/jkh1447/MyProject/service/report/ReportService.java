package com.jkh1447.MyProject.service.report;

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

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
  
  private final ReportRepository reportRepository;
  private final RoomRepository roomRepository;

  public void reportRoom(ReportDto reportDto, String roomId) {

    String currentUserId = JwtUtil.getCurrentUserId();
    if (currentUserId == null) {
      throw new UserNotFoundException();
    }

    Boolean isRoomExist = roomRepository.existsByRoomId(roomId);
    if (!isRoomExist) {
      throw new IllegalArgumentException(ReportConstants.ROOM_NOT_FOUND_MESSAGE);
    }

    if (reportDto.reason() == null || reportDto.reason().equals("") || reportDto.detail() == null || reportDto.detail().equals("")) {
      throw new IllegalArgumentException(ReportConstants.REASON_AND_DETAIL_EMPTY_MESSAGE);
    }

    if (reportDto.detail().length() > 1000) {
      throw new IllegalArgumentException(ReportConstants.REASON_AND_DETAIL_LENGTH_LIMIT_MESSAGE);
    }

    if (reportRepository.existsByReporterIdAndRoomIdAndCreatedAtAfter(currentUserId, roomId, LocalDateTime.now().minusDays(5))) {
      throw new TooManyRequestException();
    }

    Report report = Report.builder()
      .roomId(roomId)
      .reporterId(currentUserId)
      .reason(reportDto.reason())
      .detail(reportDto.detail())
      .build();

    reportRepository.save(report);
  }
}
