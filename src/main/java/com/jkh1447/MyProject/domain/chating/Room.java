package com.jkh1447.MyProject.domain.chating;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.Convert;
import com.jkh1447.MyProject.global.config.converter.StringListConverter;

@Entity
@Table(name = "rooms", indexes = { @Index(name = "idx_roomId",columnList = "roomId", unique = true) })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Room {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "room_id", nullable = false)
  private String roomId;

  @Convert(converter = StringListConverter.class)
  @Column(columnDefinition = "TEXT")
  private List<String> participants;
  
  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public Room(String roomId, List<String> participants) {
    this.roomId = roomId;
    this.participants = participants;
    this.createdAt = LocalDateTime.now();
  }

}
