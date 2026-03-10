package com.jkh1447.MyProject.domain.feedback;

import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Builder;

@Entity
@Table(name = "feedbacks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) 
@EntityListeners(AuditingEntityListener.class)
public class Feedback {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private FeedbackCategory category;

  @Column(nullable = false, length = 100)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(nullable = false)
  private String userId; 

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private FeedbackStatus status;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @Builder
    public Feedback(FeedbackCategory category, String title, String content, String userId, FeedbackStatus status) {
        this.category = category;
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.status = status;
    }

  public void setStatus(FeedbackStatus status) {
    this.status = status;
  }
}
