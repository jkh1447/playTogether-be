package com.jkh1447.MyProject.service.matching;

import org.springframework.stereotype.Service;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import com.jkh1447.MyProject.dto.matching.MatchingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkh1447.MyProject.dto.matching.QueueUser;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Collection;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchQueueService {

    /*
     * Redis Set 구조
     * 1. Active Queues // 현재 활성화된 큐는 무엇인가?
     * Key: active:queues
     * Value: {queueKey}
     * 
     * Redis ZSet 구조
     * 1. Queue // 큐에 누가 얼마나 오래 있었는가?
     * Key: {queueKey}
     * Value: {userId:score}
     * 
     * Redis Hash 구조
     * 1. User Queue Status // 유저가 어느 큐에 현재 있는가?
     * Key: user:queue:status
     * Value: {userId:queueKey}
     * 
     * 2. User Queue Infos // 유저가 어느 큐에 현재 있는가?
     * Key: user:queue:infos
     * Value: {userId:queueInfos}
     */
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private final RedisScript<Long> removeUserFromQueueScript;
    private final RedisScript<Long> removeUsersFromQueueScript;
    
    public void addToQueue(String userId, String queueKey, String queueUserInfos) {
        double score = System.currentTimeMillis();

        redisTemplate.opsForZSet().add(queueKey, userId, score);
        redisTemplate.opsForHash().put(MatchingConstants.USER_QUEUE_STATUS_KEY, userId, queueKey); // 유저가 어느 큐에 있는지 저장
        redisTemplate.opsForHash().put(MatchingConstants.USER_QUEUE_INFOS_KEY, userId, queueUserInfos); // 유저의 큐에 대한 정보(필터) 저장
        
        if (queueKey.contains(MatchingConstants.MATCH_GROUP_SIZE + "=" + MatchingConstants.ANY_GROUP_SIZE)) {
            redisTemplate.opsForSet().add(MatchingConstants.ACTIVE_ANY_QUEUE_KEY, queueKey);
        }
        else {
            redisTemplate.opsForSet().add(MatchingConstants.ACTIVE_QUEUES_KEY, queueKey);
        }
        

        log.info("[큐 참여] userId: {}, queue: {}", userId, queueKey);
    }   

    public void removeUserFromQueue(String userId, String queueKey) {

        List<String> keys = List.of(queueKey, MatchingConstants.USER_QUEUE_STATUS_KEY, MatchingConstants.USER_QUEUE_INFOS_KEY);
        redisTemplate.opsForSet().remove(MatchingConstants.ACTIVE_QUEUES_KEY, queueKey);
        redisTemplate.opsForSet().remove(MatchingConstants.ACTIVE_ANY_QUEUE_KEY, queueKey);
        redisTemplate.execute(removeUserFromQueueScript, keys, userId);
        

        log.info("[큐 나감] userId: {}, queue: {}", userId, queueKey);
    }


    public Long removeUsersFromQueue(List<QueueUser> team, String queueKey) {
        List<String> userIds = team.stream().map(QueueUser::getUserId).collect(Collectors.toList());

        List<String> keys = List.of(queueKey, MatchingConstants.USER_QUEUE_STATUS_KEY, MatchingConstants.USER_QUEUE_INFOS_KEY);

        log.info("[큐에서 유저들 제거] 유저: {}", userIds);
        
        return redisTemplate.execute(removeUsersFromQueueScript, keys, userIds.toArray());
    }

    public void cleanQueueIfEmpty(String queueKey) {
        Long size = getQueueSize(queueKey);
        if (size == null || size == 0) {
            redisTemplate.opsForSet().remove(MatchingConstants.ACTIVE_QUEUES_KEY, queueKey);
            redisTemplate.opsForSet().remove(MatchingConstants.ACTIVE_ANY_QUEUE_KEY, queueKey);
            log.debug("[큐 정리] 빈 큐 제거: {}", queueKey);
        }
    }

    public void rejoinQueue(String userId, String queueKey, double score, String userQueueInfo){
        redisTemplate.opsForZSet().add(queueKey, userId, score);
        redisTemplate.opsForHash().put(MatchingConstants.USER_QUEUE_STATUS_KEY, userId, queueKey);
        redisTemplate.opsForHash().put(MatchingConstants.USER_QUEUE_INFOS_KEY, userId, userQueueInfo);
        
        
        log.info("[큐 재참여] userId: {}, queue: {}, score: {}", userId, queueKey, score);
    }

    public String getUserQueue(String userId) {
        return (String) redisTemplate.opsForHash().get(MatchingConstants.USER_QUEUE_STATUS_KEY, userId);
    }

    public void removeUserCompletely(String userId) {
        String queueKey = getUserQueue(userId);
        if (queueKey != null) {
            removeUserFromQueue(userId, queueKey);
        }
    }

    public Long getQueueSize(String queueKey) {
        return redisTemplate.opsForZSet().size(queueKey);
    }

    private static final String PREFIX = "match";

    public String generateKey(MatchingRequest matchingRequest) {

        String groupSize = matchingRequest.filters().getOrDefault(MatchingConstants.MATCH_GROUP_SIZE, "3");

        String filterPart = matchingRequest.filters().entrySet().stream()
                .filter(entry -> !entry.getKey().equals(MatchingConstants.MATCH_GROUP_SIZE))
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        return String.format("%s:%s:groupSize=%s:%s", PREFIX, matchingRequest.gameName(), groupSize,
                filterPart);
    }

    public Long getQueueGroupSize(String queueKey) {
        return Long.parseLong(queueKey.split(":")[2].split("=")[1]);
    }

    public List<QueueUser> loadFromQueue(String queueKey, int limit) {
        
        Set<ZSetOperations.TypedTuple<Object>> userIds = redisTemplate.opsForZSet().rangeWithScores(queueKey, 0, limit - 1);

        if(userIds  == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> userIdList = new ArrayList<>();
        Map<String, Double> scoreMap = new HashMap<>(); // {id: score}

        for (ZSetOperations.TypedTuple<Object> tuple : userIds) {
            String id = String.valueOf(tuple.getValue());
            userIdList.add(id);
            scoreMap.put(id, tuple.getScore());
        }

        List<Object> queueUserInfosJsons = redisTemplate.opsForHash().multiGet(MatchingConstants.USER_QUEUE_INFOS_KEY, (Collection) userIdList);

        List<QueueUser> users = new ArrayList<>();
        for(int i=0; i<userIdList.size(); i++){
            String json = (String) queueUserInfosJsons.get(i);
            
            if (json == null) {
                // 도중에 비정상종료 / 브라우저 종료 등에 의해서 큐에서 제거된 유저인 경우
                continue;
            }

            try {
                QueueUser user = objectMapper.readValue(json, QueueUser.class);
                user.setUserId(userIdList.get(i));
                user.setQueueKey(queueKey);
                user.setScore(scoreMap.get(userIdList.get(i)));
                users.add(user);
            } catch (Exception e) {
                // 나중에 전역으로 예외처리하기
                log.error("QueueUser JSON 파싱 실패: {}", json, e);
            }

        }

        return users;
    }

    public List<QueueUser> combineAndSortPool(List<QueueUser> selfUsers, List<QueueUser> anyUsers){

        List<QueueUser> totalPool = new ArrayList<>();
        totalPool.addAll(selfUsers);
        totalPool.addAll(anyUsers);
        
        totalPool.sort(Comparator.comparing(QueueUser::getScore));

        return totalPool;
    }


}
