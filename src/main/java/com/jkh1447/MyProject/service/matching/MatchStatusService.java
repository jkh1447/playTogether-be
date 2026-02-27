package com.jkh1447.MyProject.service.matching;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.jkh1447.MyProject.dto.matching.MatchParticipant;
import com.jkh1447.MyProject.dto.matching.MatchStatusInfo;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchStatusService {

    /*
     * Redis Hash 구조 1. Match Status // 큐가 잡혔을 때 그 매치의 상태들을 저장 Key: {matchId} Value: {groupSize:int,
     * acceptCount:int, participants:String, queueKey:String} + {"user:{userId}":"ACCEPTED"}(수락시 생성)
     */

    private final RedisTemplate<String, Object> redisTemplate;

    private final RedisScript<Long> incrementAcceptCountScript;
    private final RedisScript<List> processMatchTimeoutScript;

    private final StringRedisTemplate stringRedisTemplate;

    public void createMatchStatus(String matchId, MatchStatusInfo matchStatusInfo,
            int expireSeconds) {
        String statusKey = buildStatusKey(matchId);

        Map<String, Object> matchStatus = new HashMap<>();
        matchStatus.put(MatchingConstants.MATCH_GROUP_SIZE,
                Integer.parseInt(matchStatusInfo.groupSize()));
        matchStatus.put(MatchingConstants.MATCH_ACCEPT_COUNT, 0);
        matchStatus.put(MatchingConstants.MATCH_PARTICIPANTS_DATA,
                matchStatusInfo.participantsToRedisFormat());
        matchStatus.put(MatchingConstants.MATCH_QUEUE_KEY, matchStatusInfo.queueKey());

        redisTemplate.opsForHash().putAll(statusKey, matchStatus);
        redisTemplate.expire(statusKey, Duration.ofSeconds(expireSeconds)); // matchStatus만료시간

        log.info("[매칭 상태 생성] matchId: {}, groupSize: {}, participants: {}", matchId,
                matchStatusInfo.groupSize(), matchStatusInfo.getParticipantIds());
    }

    private String buildStatusKey(String matchId) {
        return MatchingConstants.MATCH_STATUS_KEY + matchId;
    }

    public MatchStatusInfo getMatchStatus(String matchId) {
        String statusKey = buildStatusKey(matchId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(statusKey);

        return convertToMatchStatusInfo(entries);
    }

    public MatchStatusInfo getMatchStatus(List<Object> data) {

        Map<Object, Object> entries = new HashMap<>();
        for (int i = 0; i < data.size(); i += 2) {
            entries.put(String.valueOf(data.get(i)), String.valueOf(data.get(i + 1)));
        }

        return convertToMatchStatusInfo(entries);
    }

    private MatchStatusInfo convertToMatchStatusInfo(Map<Object, Object> entries) {
        if (entries == null || entries.isEmpty()) {
            return null;
        }


        Object rawGroupSize = entries.get(MatchingConstants.MATCH_GROUP_SIZE);
        Object rawAcceptCount = entries.get(MatchingConstants.MATCH_ACCEPT_COUNT);
        String participantsData = (String) entries.get(MatchingConstants.MATCH_PARTICIPANTS_DATA);
        String queueKey = (String) entries.get(MatchingConstants.MATCH_QUEUE_KEY);

        Map<String, String> acceptedUsers = new HashMap<>();

        entries.forEach((key, value) -> {
            if (key.toString().startsWith(MatchingConstants.MATCH_ACCEPTED_PREFIX)) {
                acceptedUsers.put(key.toString(), value.toString());
            }
        });


        if (rawGroupSize == null || participantsData == null) {
            return null;
        }

        String groupSize = rawGroupSize.toString();
        int acceptCount = rawAcceptCount != null ? Integer.parseInt(rawAcceptCount.toString()) : 0;
        List<MatchParticipant> participants = parseParticipants(participantsData);

        return MatchStatusInfo.builder().groupSize(groupSize).acceptCount(acceptCount)
                .participants(participants).queueKey(queueKey).acceptedUsers(acceptedUsers).build();
    }

    private List<MatchParticipant> parseParticipants(String participantsData) {
        if (participantsData == null || participantsData.isEmpty()) {
            return List.of();
        }

        String cleanData = participantsData.substring(1, participantsData.length() - 1);

        return Arrays.stream(cleanData.split(";")).map(MatchParticipant::fromRedisFormat)
                .collect(Collectors.toList());
    }

    /**
     * 수락 카운트 증가
     * 
     * @return 1: 매칭 성사, 0: 매칭 진행중, -1: 중복 수락, -2: 매칭 없음
     */
    public Long incrementAcceptCount(String matchId, String userId) {
        String statusKey = buildStatusKey(matchId);

        Long result = redisTemplate.execute(incrementAcceptCountScript, List.of(statusKey), // KEYS[1]
                userId // ARGV[1]
        );

        return result;
    }

    public boolean markAsDeclined(String matchId) {
        String statusKey = buildStatusKey(matchId);

        Boolean isNewDecline = redisTemplate.opsForHash().putIfAbsent(statusKey,
                MatchingConstants.MATCH_DECLINED_FLAG, true);

        if (Boolean.FALSE.equals(isNewDecline)) {
            log.warn("[중복 거절 방지] matchId: {}", matchId);
            return false;
        }

        log.info("[매칭 거절] matchId: {}", matchId);
        return true;
    }

    public boolean deleteMatchStatus(String matchId) {
        String statusKey = buildStatusKey(matchId);
        Boolean isDeleted = redisTemplate.delete(statusKey);

        if (Boolean.TRUE.equals(isDeleted)) {
            log.info("[매칭 상태 삭제] matchId: {}", matchId);
        }

        return Boolean.TRUE.equals(isDeleted);
    }

    public List<Object> getMatchStatusAtomically(String matchId) {
        String statusKey = buildStatusKey(matchId);

        return stringRedisTemplate.execute(processMatchTimeoutScript, List.of(statusKey));
    }
}
