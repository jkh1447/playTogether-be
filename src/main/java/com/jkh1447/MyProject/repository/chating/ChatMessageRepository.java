package com.jkh1447.MyProject.repository.chating;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import com.jkh1447.MyProject.domain.chating.ChatMessage;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
  
  @Modifying(clearAutomatically = true)
  @Query("DELETE FROM ChatMessage m WHERE " +
        "(m.isPreserved = false AND m.createdAt < :threeMonthsAgo) OR " +
        "(m.isPreserved = true AND m.createdAt < :sixMonthsAgo)")
  int deleteByPolicy(@Param("threeMonthsAgo") LocalDateTime threeMonthsAgo, 
                    @Param("sixMonthsAgo") LocalDateTime sixMonthsAgo);
                   
  List<ChatMessage> findByRoomIdOrderByCreatedAtDesc(String roomId);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE ChatMessage m SET m.isPreserved = true WHERE m.roomId = :roomId")
  void updateIsPreservedByRoomId(@Param("roomId") String roomId);
}
