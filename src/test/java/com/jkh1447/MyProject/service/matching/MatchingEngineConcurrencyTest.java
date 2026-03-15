package com.jkh1447.MyProject.service.matching;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class MatchingEngineConcurrencyTest {

    @Autowired
    private MatchingEngineService matchingEngineService;

    @Autowired
    private MatchQueueService matchQueueService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    @AfterEach
    public void cleanup() {
        // 테스트 전후로 Redis 상태 초기화
        redisTemplate.delete(MatchingConstants.ACTIVE_QUEUES_KEY);
        redisTemplate.delete(MatchingConstants.ACTIVE_ANY_QUEUE_KEY);
        redisTemplate.delete(MatchingConstants.USER_QUEUE_STATUS_KEY);
        redisTemplate.delete(MatchingConstants.USER_QUEUE_INFOS_KEY);
        
        Set<String> statusKeys = redisTemplate.keys(MatchingConstants.MATCH_STATUS_KEY + "*");
        if (statusKeys != null && !statusKeys.isEmpty()) {
            redisTemplate.delete(statusKeys);
        }
        Set<String> queueKeys = redisTemplate.keys("queue:*");
        if (queueKeys != null && !queueKeys.isEmpty()) {
            redisTemplate.delete(queueKeys);
        }
        Set<String> matchKeys = redisTemplate.keys("match:*");
        if (matchKeys != null && !matchKeys.isEmpty()) {
            redisTemplate.delete(matchKeys);
        }
    }

    @Test
    @DisplayName("동시에 여러 스레드가 매칭 엔진을 실행해도 중복 매칭이 발생하지 않는다")
    public void testConcurrentMatchingExecution() throws InterruptedException, JsonProcessingException {
        // given
        // 큐 이름 설정 (userCreated 게임, 필요 인원 3명)
        String queueKey = "queue:userCreated:groupSize=3:testRoom";

        // 사용자 큐 정보 JSON 구조 세팅
        Map<String, Object> wrapper = Map.of("userInfo", Map.of());
        String infoJson = objectMapper.writeValueAsString(wrapper);

        // 3명의 유저를 대기열(Queue)에 등록하여 정확히 1번의 매칭 조건(3명)을 만족시킴
        matchQueueService.addToQueue("test_user_1", queueKey, infoJson);
        matchQueueService.addToQueue("test_user_2", queueKey, infoJson);
        matchQueueService.addToQueue("test_user_3", queueKey, infoJson);

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        // 10개의 스레드가 동시에 매칭 처리(processMatching)를 시도
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    matchingEngineService.processMatching();
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 스레드의 작업이 끝날 때까지 대기
        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // then
        // 1. 대기열(Queue)에 남은 유저가 없어야 한다. (모두 매치되어 Queue에서 빠져나가야 함)
        Long queueSize = matchQueueService.getQueueSize(queueKey);
        assertThat(queueSize).isZero();

        // 2. 여러 스레드가 동시에 매칭을 시도했지만 분산락(Redisson)을 통해서 정확히 1개의 매칭만 생성되어야 한다.
        Set<String> matchStatusKeys = redisTemplate.keys(MatchingConstants.MATCH_STATUS_KEY + "*");
        assertThat(matchStatusKeys).hasSize(1);
    }
}
