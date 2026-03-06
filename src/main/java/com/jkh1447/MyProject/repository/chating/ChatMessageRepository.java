package com.jkh1447.MyProject.repository.chating;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.jkh1447.MyProject.domain.chating.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
  
  long deleteByCreatedAtBefore(LocalDateTime createdAt);
}
