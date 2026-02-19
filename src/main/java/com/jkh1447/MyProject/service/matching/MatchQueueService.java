package com.jkh1447.MyProject.service.matching;

import org.springframework.stereotype.Service;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import com.jkh1447.MyProject.dto.matching.MatchingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.RedisTemplate;

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
     */
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public void addToQueue(String userId, String queueKey) {
        double score = System.currentTimeMillis();

        redisTemplate.opsForZSet().add(queueKey, userId, score);
        redisTemplate.opsForHash().put(MatchingConstants.USER_QUEUE_STATUS_KEY, userId, queueKey);
        redisTemplate.opsForSet().add(MatchingConstants.ACTIVE_QUEUES_KEY, queueKey);

        log.info("[큐 참여] userId: {}, queue: {}", userId, queueKey);
    }   

    public void removeFromQueue(String userId, String queueKey) {
        redisTemplate.opsForZSet().remove(queueKey, userId);
        redisTemplate.opsForHash().delete(MatchingConstants.USER_QUEUE_STATUS_KEY, userId);
        
        cleanQueueIfEmpty(queueKey);

        log.info("[큐 나감] userId: {}, queue: {}", userId, queueKey);
    }

    public void cleanQueueIfEmpty(String queueKey) {
        Long size = getQueueSize(queueKey);
        if (size == null || size == 0) {
            redisTemplate.opsForSet().remove(MatchingConstants.ACTIVE_QUEUES_KEY, queueKey);
            log.debug("[큐 정리] 빈 큐 제거: {}", queueKey);
        }
    }

    public void rejoinQueue(String userId, String queueKey, double score){
        redisTemplate.opsForZSet().add(queueKey, userId, score);
        redisTemplate.opsForHash().put(MatchingConstants.USER_QUEUE_STATUS_KEY, userId, queueKey);
        
        log.info("[큐 재참여] userId: {}, queue: {}, score: {}", userId, queueKey, score);
    }

    public String getUserQueue(String userId) {
        return (String) redisTemplate.opsForHash().get(MatchingConstants.USER_QUEUE_STATUS_KEY, userId);
    }

    public void removeUserCompletely(String userId) {
        String queueKey = getUserQueue(userId);
        if (queueKey != null) {
            removeFromQueue(userId, queueKey);
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
}
