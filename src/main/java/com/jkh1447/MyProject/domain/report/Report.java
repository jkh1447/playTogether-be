package com.jkh1447.MyProject.domain.report;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roomId;        // 채팅방 ID

    @Column(nullable = false)
    private String reporterId;    // 신고자 ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;  // 신고 사유

    @Column(nullable = false, length = 1000)
    private String detail;        // 신고 내용 (1000자 제한)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt; // 신고 접수 시간 (운영 확인용)

    @Builder
    public Report(String roomId, String reporterId, ReportReason reason, String detail, ReportStatus status) {
        this.roomId = roomId;
        this.reporterId = reporterId;
        this.reason = reason;
        this.detail = detail;
        this.status = status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }
}