package com.jkh1447.MyProject.repository.feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jkh1447.MyProject.domain.feedback.Feedback;

public interface feedbackRepository extends JpaRepository<Feedback, Long> {
  
}
