package com.jkh1447.MyProject.repository.matching;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;
import com.jkh1447.MyProject.dto.matching.QueueLog;

public interface QueueLogRepository extends JpaRepository<QueueLog, String> {
  @Modifying
  @Transactional
  @Query(value = "INSERT INTO queue_log (queue_key, count, last_access_time) " +
                  "VALUES (:key, 1, CURRENT_TIMESTAMP) " +
                  "ON CONFLICT (queue_key) " +
                  "DO UPDATE SET count = queue_log.count + 1, " +
                  "              last_access_time = CURRENT_TIMESTAMP", 
          nativeQuery = true)
  void upsertQueueKey(@Param("key") String key);

}
