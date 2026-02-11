package com.jkh1447.MyProject.service.matching;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.jkh1447.MyProject.dto.matching.MatchCompleteResponse;
import com.jkh1447.MyProject.dto.matching.MatchDeclineResponse;
import com.jkh1447.MyProject.dto.matching.MatchFoundResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import org.springframework.context.annotation.Lazy;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    // public MatchingNotificationService(@Lazy SimpMessagingTemplate messagingTemplate) {
    //     this.messagingTemplate = messagingTemplate;
    // }

    // 스프링이 알아서 소켓을 연결할때 기록했던 userId의 주소로 메세지를 전송함.
    public void sendMatchFound(String userId, String matchId) {

        MatchFoundResponse matchFoundResponse = MatchFoundResponse.builder()
            .matchId(matchId)
            .timeoutSeconds(15)
            .build();

        // user/{userId}/queue/match-found 주소로 matchId를 보냄
        messagingTemplate.convertAndSendToUser(
            userId, 
            MatchingConstants.SUB_MATCH_FOUND_PATH, 
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
            MatchingConstants.SUB_MOVE_ROOM_PATH,
            matchCompleteResponse
        );
    }

    public void sendDeclineMatch(String userId, String matchId, MatchDeclineResponse.Status status) {
        MatchDeclineResponse matchDeclineResponse = MatchDeclineResponse.builder()
            .matchId(matchId)
            .status(status)
            .build();

        messagingTemplate.convertAndSendToUser(
            userId,
            MatchingConstants.SUB_MATCH_DECLINE_PATH,
            matchDeclineResponse
        );
    }
}