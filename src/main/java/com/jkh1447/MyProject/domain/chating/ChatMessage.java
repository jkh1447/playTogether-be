package com.jkh1447.MyProject.domain.chating;

import org.springframework.data.annotation.CreatedDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "chat_messages", indexes = {
  @Index(name = "idx_room_created_at", columnList = "roomId, createdAt"),
  @Index(name = "idx_preserved_created_at", columnList = "isPreserved, createdAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String roomId;

  @Column(nullable = false)
  private String senderId;

  @Column(nullable = false)
  private String senderNickname;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;

  @Column(length = 45, nullable = true)
  private String clientIp;

  @Column(length = 512, updatable = false, nullable = true)
  private String userAgent;

  @Column(nullable = false)
  private Boolean isPreserved = false;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public ChatMessage(String roomId, String senderId, String senderNickname, String content, String clientIp, String userAgent) {
    this.roomId = roomId;
    this.senderId = senderId;
    this.senderNickname = senderNickname;
    this.content = content;
    this.clientIp = clientIp;
    this.userAgent = userAgent;
  }

  public void preserve() {
    this.isPreserved = true;
  }

}