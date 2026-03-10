package com.jkh1447.MyProject.repository.feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jkh1447.MyProject.domain.feedback.Feedback;
import com.jkh1447.MyProject.domain.feedback.FeedbackCategory;
import com.jkh1447.MyProject.domain.feedback.FeedbackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface feedbackRepository extends JpaRepository<Feedback, Long> {
  // 카테고리와 상태로 필터링 + 페이징
    // Spring Data JPA가 메서드 이름을 분석하여 WHERE 절과 LIMIT/OFFSET을 자동으로 생성합니다.
    Page<Feedback> findByCategoryAndStatus(FeedbackCategory category, FeedbackStatus status, Pageable pageable);
    
    // 필터 조건이 없거나 카테고리만 있는 경우 등을 위해 메서드 오버로딩
    Page<Feedback> findByCategory(FeedbackCategory category, Pageable pageable);
    Page<Feedback> findByStatus(FeedbackStatus status, Pageable pageable);
}
