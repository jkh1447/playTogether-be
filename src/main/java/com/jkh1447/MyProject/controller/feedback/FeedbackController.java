package com.jkh1447.MyProject.controller.feedback;

import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.jkh1447.MyProject.dto.feedback.FeedbackDto;
import com.jkh1447.MyProject.dto.feedback.UpdateFeedbackStatusDto;
import com.jkh1447.MyProject.global.response.ApiResponse;
import com.jkh1447.MyProject.service.feedback.FeedbackService;
import com.jkh1447.MyProject.security.JwtUtil;
import java.beans.PropertyEditorSupport;
import java.util.List;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import com.jkh1447.MyProject.dto.feedback.FeedbackCategoryDto;
import com.jkh1447.MyProject.domain.feedback.Feedback;
import com.jkh1447.MyProject.domain.feedback.FeedbackCategory;
import com.jkh1447.MyProject.domain.feedback.FeedbackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/feedback")
@Slf4j
@RequiredArgsConstructor
public class FeedbackController {
  
  private final FeedbackService feedbackService;

  @PostMapping("/submit")
  public void submitFeedback(@RequestBody FeedbackDto feedbackDto) {
    String userId = JwtUtil.getCurrentUserId();
    feedbackDto.setUserId(userId);
    feedbackService.submitFeedback(feedbackDto, userId);
    log.info("피드백 제출 성공: {}", feedbackDto);
  }

  @GetMapping("/category")
  public ResponseEntity<ApiResponse<?>> getFeedbackCategories() {
    List<FeedbackCategoryDto> categories = feedbackService.getFeedbackCategories();
    return ResponseEntity.ok()
      .body(ApiResponse.success(categories)); 
  }

  @GetMapping("/length")
  public ResponseEntity<ApiResponse<?>> getFeedbackLength() {
    String length = String.valueOf(feedbackService.getFeedbackLength());
    return ResponseEntity.ok()
    .body(ApiResponse.success(length));
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(FeedbackStatus.class, new PropertyEditorSupport() {
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            setValue(FeedbackStatus.fromValue(text));
        }
    });

    binder.registerCustomEditor(FeedbackCategory.class, new PropertyEditorSupport() {
        @Override
        public void setAsText(String text) {
            setValue(FeedbackCategory.fromValue(text));
        }
    });
  }

  @GetMapping
  public ResponseEntity<ApiResponse<?>> getFeedbacks(
    @RequestParam(required = false) FeedbackCategory category,
    @RequestParam(required = false) FeedbackStatus status,
    @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    Page<Feedback> feedbackPage = feedbackService.getFeedbackList(category, status, pageable);
    log.info("요청 페이지: {}, 카테고리: {}, 상태: {}", pageable.getPageNumber(), category, status);
    log.info("결과 : {}", feedbackPage.getContent());
    return ResponseEntity.ok(ApiResponse.success(feedbackPage));
  }

  @PatchMapping("/update/status")
  public ResponseEntity<ApiResponse<?>> updateFeedbackStatus(@RequestBody UpdateFeedbackStatusDto updateFeedbackStatusDto) {
    feedbackService.updateFeedbackStatus(updateFeedbackStatusDto);
    return ResponseEntity.ok(ApiResponse.success("피드백 상태 업데이트 성공"));
  }
}
