package com.jkh1447.MyProject.service.matching;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.jkh1447.MyProject.dto.matching.MatchParticipant;
import com.jkh1447.MyProject.dto.matching.MatchStatusInfo;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchStatusService {

    /*
     * Redis Hash 구조 
     * 1. Match Status // 큐가 잡혔을 때 그 매치의 상태들을 저장
     * Key: {matchId}
     * Value: {groupSize:int, acceptCount:int, participants:String, queueKey:String}
     */

    private final RedisTemplate<String, Object> redisTemplate;

    public void createMatchStatus(String matchId, MatchStatusInfo matchStatusInfo,
            int expireSeconds) {
        String statusKey = buildStatusKey(matchId);

        Map<String, Object> matchStatus = new HashMap<>();
        matchStatus.put(MatchingConstants.MATCH_GROUP_SIZE, matchStatusInfo.groupSize());
        matchStatus.put(MatchingConstants.MATCH_ACCEPT_COUNT, 0);
        matchStatus.put(MatchingConstants.MATCH_PARTICIPANTS_DATA,
                matchStatusInfo.participantsToRedisFormat());
        matchStatus.put(MatchingConstants.MATCH_QUEUE_KEY, matchStatusInfo.queueKey());

        redisTemplate.opsForHash().putAll(statusKey, matchStatus);
        redisTemplate.expire(statusKey, Duration.ofSeconds(expireSeconds));

        log.info("[매칭 상태 생성] matchId: {}, groupSize: {}, participants: {}",  matchId,
                matchStatusInfo.groupSize(), matchStatusInfo.getParticipantIds());
    }

    private String buildStatusKey(String matchId) {
        return MatchingConstants.MATCH_STATUS_KEY + matchId;
    }

    public MatchStatusInfo getMatchStatus(String matchId) {
        String statusKey = buildStatusKey(matchId);

        Object rawGroupSize =
                redisTemplate.opsForHash().get(statusKey, MatchingConstants.MATCH_GROUP_SIZE);
        Object rawAcceptCount =
                redisTemplate.opsForHash().get(statusKey, MatchingConstants.MATCH_ACCEPT_COUNT);
        String participantsData = (String) redisTemplate.opsForHash().get(statusKey,
                MatchingConstants.MATCH_PARTICIPANTS_DATA);
        String queueKey = (String) redisTemplate.opsForHash().get(statusKey,
                MatchingConstants.MATCH_QUEUE_KEY);

        if (rawGroupSize == null || participantsData == null) {
            return null;
        }

        int groupSize = Integer.parseInt(rawGroupSize.toString());
        int acceptCount = rawAcceptCount != null ? Integer.parseInt(rawAcceptCount.toString()) : 0;
        List<MatchParticipant> participants = parseParticipants(participantsData);

        return MatchStatusInfo.builder().groupSize(groupSize).acceptCount(acceptCount)
                .participants(participants).queueKey(queueKey).build();
    }

    private List<MatchParticipant> parseParticipants(String participantsData) {
        if (participantsData == null || participantsData.isEmpty()) {
            return List.of();
        }

        return Arrays.stream(participantsData.split(",")).map(MatchParticipant::fromRedisFormat)
                .collect(Collectors.toList());
    }

    /**
     * 수락 카운트 증가
     * 
     * 기존 위치: MatchingService.acceptMatch (66-74줄)
     * 
     * @return 새로 수락된 경우 true, 이미 수락한 경우 false
     */
    public boolean incrementAcceptCount(String matchId, String userId) {
        String statusKey = buildStatusKey(matchId);

        Boolean isNewAccept = redisTemplate.opsForHash().putIfAbsent(statusKey,
                MatchingConstants.MATCH_ACCEPTED_PREFIX + userId,
                MatchingConstants.MATCH_STATUS_ACCEPTED);

        if (Boolean.FALSE.equals(isNewAccept)) {
            log.warn("[중복 수락 방지] userId: {}, matchId: {}", userId, matchId);
            return false;
        }

        redisTemplate.opsForHash().increment(statusKey, MatchingConstants.MATCH_ACCEPT_COUNT, 1);
        log.info("[매칭 수락] userId: {}, matchId: {}", userId, matchId);
        return true;
    }

    public boolean markAsDeclined(String matchId) {
        String statusKey = buildStatusKey(matchId);

        Boolean isNewDecline = redisTemplate.opsForHash().putIfAbsent(statusKey,
                MatchingConstants.MATCH_DECLINED_FLAG, true);
        
        if(Boolean.FALSE.equals(isNewDecline)) {
            log.warn("[중복 거절 방지] matchId: {}", matchId);
            return false;
        }

        log.info("[매칭 거절] matchId: {}", matchId);
        return true;
    }

    public boolean deleteMatchStatus(String matchId){
        String statusKey = buildStatusKey(matchId);
        Boolean isDeleted = redisTemplate.delete(statusKey);

        if (Boolean.TRUE.equals(isDeleted)) {
            log.info("[매칭 상태 삭제] matchId: {}", matchId);
        }
        
        return Boolean.TRUE.equals(isDeleted);
    }

    
}
