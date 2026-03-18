package com.jkh1447.MyProject.dto.matching;

import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QueueLog {
  @Id
  @Column(length=100)
  private String queueKey;

  @Builder.Default
  @Column(nullable = false)
  private Long count = 1L;

  @UpdateTimestamp
  private LocalDateTime lastAccessTime;
  
  public QueueLog(String queueKey) {
    this.queueKey = queueKey;
  }

  public void incrementCount() {
    this.count++;
  }
}
