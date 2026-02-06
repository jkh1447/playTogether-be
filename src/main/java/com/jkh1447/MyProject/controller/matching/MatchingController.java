package com.jkh1447.MyProject.controller.matching;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.jkh1447.MyProject.service.matching.MatchingService;
import lombok.RequiredArgsConstructor;
import com.jkh1447.MyProject.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import com.jkh1447.MyProject.global.response.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.jkh1447.MyProject.dto.matching.MatchAcceptRequest;
import com.jkh1447.MyProject.dto.matching.MatchingRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<String>> join(@RequestBody MatchingRequest request) {
        String userId = JwtUtil.getCurrentUserId();
        matchingService.joinQueue(userId, request);
        return ResponseEntity.ok(ApiResponse.success("매칭 큐에 성공적으로 참여했습니다."));
    }

    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<String>> accept(@RequestBody MatchAcceptRequest request) {
        String userId = JwtUtil.getCurrentUserId();
        matchingService.acceptMatch(userId, request.matchId());
        log.info("매칭 수락 matchId: {}", request.matchId());
        return ResponseEntity.ok(ApiResponse.success("매칭 수락에 성공했습니다."));
    }

}
