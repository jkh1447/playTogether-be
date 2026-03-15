package com.jkh1447.MyProject.service.matching;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.jkh1447.MyProject.dto.matching.MatchParticipant;
import com.jkh1447.MyProject.dto.matching.MatchStatusInfo;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import org.springframework.data.redis.core.script.RedisScript;
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

    private final RedisScript<List> incrementAcceptCountScript;
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

        // 여기서 들고올때, jackson serializer로 인해 string으로 들고오게 된다. 그래서 여기 파싱에는 문제가 없다. (수락하는 경우.)

        return convertToMatchStatusInfo(entries);
    }

    public MatchStatusInfo getMatchStatus(List<Object> data) {

        Map<Object, Object> entries = new HashMap<>();
        for (int i = 0; i < data.size(); i += 2) {
            String key = String.valueOf(data.get(i));
            String value = String.valueOf(data.get(i + 1));

            // 💡 participants 키인 경우에만 양쪽 따옴표 제거
            if ("participantsData".equals(key) && value != null) {
                value = value.replace("\"", "");
            }

            entries.put(key, value);
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
        if (participantsData != null) {
            participantsData = participantsData.replace("\"", "");
        }
        String queueKey = (String) entries.get(MatchingConstants.MATCH_QUEUE_KEY);
        if (queueKey != null) {
            // 따옴표를 빈 문자열로 치환
            queueKey = queueKey.replace("\"", ""); 
        }
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

        return Arrays.stream(participantsData.split(",")).map(MatchParticipant::fromRedisFormat)
                .collect(Collectors.toList());
    }

    /**
     * 수락 카운트 증가
     * 
     * @return 1: 매칭 성사, 0: 매칭 진행중, -1: 중복 수락, -2: 매칭 없음
     */
    public List<Object> incrementAcceptCount(String matchId, String userId) {
        String statusKey = buildStatusKey(matchId);

        List<Object> result = stringRedisTemplate.execute(incrementAcceptCountScript, List.of(statusKey), userId);

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
