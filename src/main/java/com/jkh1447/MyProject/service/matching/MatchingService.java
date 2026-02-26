package com.jkh1447.MyProject.service.matching;

import java.util.UUID;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.jkh1447.MyProject.dto.matching.MatchingRequest;
import com.jkh1447.MyProject.service.matching.strategy.MatchStrategy;
import com.jkh1447.MyProject.service.matching.strategy.MatchStrategyFactory;
import com.jkh1447.MyProject.dto.matching.MatchDeclineResponse;
import com.jkh1447.MyProject.dto.matching.MatchParticipant;
import com.jkh1447.MyProject.dto.matching.MatchStatusInfo;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MatchingNotificationService notificationService;
    private final MatchQueueService queueService;
    private final MatchStatusService matchStatusService;
    private final RoomService roomService;
    private final MatchStrategyFactory strategyFactory;

    public void joinQueue(String userId, MatchingRequest request) {
        MatchStrategy strategy = strategyFactory.getStrategy(request.gameName());
        String queueKey = strategy.generateQueueKey(request);
        String queueUserInfos = strategy.getQueueUserInfos(request);
        queueService.addToQueue(userId, queueKey, queueUserInfos);
    }

    public void leaveQueue(String userId, MatchingRequest request) {
        MatchStrategy strategy = strategyFactory.getStrategy(request.gameName());
        String queueKey = strategy.generateQueueKey(request);

        queueService.removeUserFromQueue(userId, queueKey);

        queueService.cleanQueueIfEmpty(queueKey);

        log.info("[매칭 큐 나가기] userId: {}, game: {}", userId, request.gameName());
    }

    public void acceptMatch(String userId, String matchId) {

        Long result = matchStatusService.incrementAcceptCount(matchId, userId);

        if (result == null) {
            // 이미 삭제된 경우
            return;
        }

        if (result == -1) {
            log.warn("[수락 실패] 이미 수락했거나 만료된 매칭. userId: {}, matchId: {}", userId, matchId);
            return;
        }
        if (result == -2) {
            log.warn("[수락 실패] 매칭이 존재하지 않습니다. userId: {}, matchId: {}", userId, matchId);
            return;
        }
        if (result == 0) {
            log.info("[매칭 진행중] userId: {}, matchId: {}", userId, matchId);
            return;
        }

        if (result == 1) {
            MatchStatusInfo statusInfo = matchStatusService.getMatchStatus(matchId);
            if (statusInfo == null) {
                log.warn("⚠️ 만료되었거나 존재하지 않는 매칭입니다: {}", matchId);
                return;
            }

            completeMatch(matchId, statusInfo);
        }

    }

    private void completeMatch(String matchId, MatchStatusInfo statusInfo) {
        String roomId = UUID.randomUUID().toString();

        // 방 생성 & roomStatus 생성
        roomService.createRoom(roomId, statusInfo.participants());

        // 모든 참가자들에게 방 이동 알림
        for (String userId : statusInfo.getParticipantIds()) {
            notificationService.sendMoveToRoom(userId, roomId);
        }

        // 매칭 상태 삭제
        matchStatusService.deleteMatchStatus(matchId);

        log.info("✅ [매칭 성사] matchId: {}, roomId: {}", matchId, roomId);
    }

    public void declineMatch(String userId, String matchId) {

        boolean declined = matchStatusService.markAsDeclined(matchId);
        if (!declined) {
            log.warn("[거절 실패] 이미 거절되었거나 만료된 매칭. userId: {}, matchId: {}", userId, matchId);
            return;
        }

        MatchStatusInfo statusInfo = matchStatusService.getMatchStatus(matchId);
        if (statusInfo == null) {
            log.warn("⚠️ 만료되었거나 존재하지 않는 매칭입니다: {}", matchId);
            return;
        }


        handleMatchCancellation(userId, matchId, statusInfo);

        matchStatusService.deleteMatchStatus(matchId);

        log.info("❌ [매칭 거절] matchId: {}, 거절자: {}", matchId, userId);
    }

    private void handleMatchCancellation(String rejectUserId, String matchId,
            MatchStatusInfo statusInfo) {

        for (MatchParticipant participant : statusInfo.participants()) {
            String participantId = participant.userId();

            if (participantId.equals(rejectUserId)) {
                notificationService.sendDeclineMatch(participantId, matchId,
                        MatchDeclineResponse.Status.REJECTED);
            } else {
                notificationService.sendDeclineMatch(participantId, matchId,
                        MatchDeclineResponse.Status.CANCELLED);
                queueService.rejoinQueue(participantId, statusInfo.queueKey(), participant.score(), participant.infos());
            }
        }
    }

    public void removeUserFromQueue(String userId) {

        queueService.removeUserCompletely(userId);

        log.info("[Redis Delete] 비정상 종료 유저 큐 제거 완료: {}", userId);
    }



}
