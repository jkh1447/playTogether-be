package com.jkh1447.MyProject.service.matching;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.jkh1447.MyProject.dto.matching.MatchCompleteResponse;
import com.jkh1447.MyProject.dto.matching.MatchFoundResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    // 스프링이 알아서 소켓을 연결할때 기록했던 userId의 주소로 메세지를 전송함.
    public void sendMatchFound(String userId, String matchId) {

        MatchFoundResponse matchFoundResponse = MatchFoundResponse.builder()
            .matchId(matchId)
            .timeoutSeconds(15)
            .build();

        // /queue/user1/match-found 주소로 matchId를 보냄
        messagingTemplate.convertAndSendToUser(
            userId, 
            "/queue/match-found", 
            matchFoundResponse
        );

        log.info("매칭 성공 알림 전송: userId={}, matchId={}", userId, matchId);
    }

    public void sendMoveToRoom(String userId, String roomId) {
        MatchCompleteResponse matchCompleteResponse = MatchCompleteResponse.builder()
            .roomId(roomId)
            .status("MATCH_COMPLETE")
            .build();

        messagingTemplate.convertAndSendToUser(
            userId,
            "/queue/move-room",
            matchCompleteResponse
        );
    }
}