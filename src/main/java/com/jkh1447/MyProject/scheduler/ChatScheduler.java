package com.jkh1447.MyProject.scheduler;

import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.jkh1447.MyProject.repository.chating.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatScheduler {
  
  private final ChatMessageRepository chatMessageRepository;

  @Scheduled(cron = "0 0 3 * * *")
  @Transactional
  public void deleteOldChatLogs() {
      LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
      
      log.info("채팅 로그 삭제 스케줄러 시작: {} 이전 데이터 삭제", threeMonthsAgo);
      
      // 3개월 이전 데이터 삭제
      long deletedCount = chatMessageRepository.deleteByCreatedAtBefore(threeMonthsAgo);
      
      log.info("채팅 로그 삭제 완료. 삭제된 로그 수: {}건", deletedCount);
  }
}
