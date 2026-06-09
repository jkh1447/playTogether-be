package com.jkh1447.MyProject.service.matching;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.redis.core.RedisTemplate;

import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import com.jkh1447.MyProject.dto.matching.MatchParticipant;
import com.jkh1447.MyProject.dto.matching.MatchStatusInfo;

import java.lang.reflect.Method;
import java.util.List;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("local")
public class MatchingAcceptConcurrencyTest {

    @Autowired
    private MatchingService matchingService;

    @Autowired
    private MatchStatusService matchStatusService;

    @Autowired
    private MatchingEngineService matchingEngineService;

    @MockitoSpyBean
    private RoomService roomService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    @AfterEach
    public void cleanup() {
        Set<String> statusKeys = redisTemplate.keys(MatchingConstants.MATCH_STATUS_KEY + "*");
        if (statusKeys != null && !statusKeys.isEmpty()) {
            redisTemplate.delete(statusKeys);
        }
        
        // 이전 큐 정보 초기화
        Set<String> queueKeys = redisTemplate.keys("queue:*");
        if (queueKeys != null && !queueKeys.isEmpty()) {
            redisTemplate.delete(queueKeys);
        }

        redisTemplate.delete(MatchingConstants.USER_QUEUE_STATUS_KEY);
        redisTemplate.delete(MatchingConstants.USER_QUEUE_INFOS_KEY);
    }

    @Test
    @DisplayName("2명의 유저가 동시에 마지막 수락을 여러 번 요청(클라이언트 따닥 등)할 때 방(Room)이 한 개만 생성된다.")
    public void testConcurrentAcceptanceDuplicateRoomCreation() throws InterruptedException {
        // given
        String matchId = UUID.randomUUID().toString();
        String userId1 = "userA";
        String userId2 = "userB";
        String queueKey = "queue:testRoom";

        List<MatchParticipant> participants = List.of(
                MatchParticipant.builder().userId(userId1).score(100.0).nickname("UserA").build(),
                MatchParticipant.builder().userId(userId2).score(100.0).nickname("UserB").build()
        );

        MatchStatusInfo statusInfo = MatchStatusInfo.builder()
                .groupSize("2")
                .acceptCount(0)
                .participants(participants)
                .queueKey(queueKey)
                .acceptedUsers(new HashMap<>())
                .build();

        // Redis 매칭 상태 생성 (만료시간 10초 명시)
        matchStatusService.createMatchStatus(matchId, statusInfo, 10);

        // UserA 가 정상적으로 1번째 수락 (Race Condition 미발생 일반 케이스)
        matchingService.acceptMatch(userId1, matchId);

        // UserB 의 요청이 동시다발적으로 (예측 불가능하게) 10회 들어왔다고 가정
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    matchingService.acceptMatch(userId2, matchId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        // then
        // Redis의 Lua Script(incrementAcceptCountScript) 가 원자적으로 연산하기 때문에
        // acceptCount 가 groupSize 와 일치하는 구간이 오직 '한 번'만 통과해야 한다.
        // 그러므로 RoomService 의 createRoom 은 10번 중 단 1회만 실행됨.
        verify(roomService, times(1)).createRoom(anyString(), any());

        // 매칭이 완료되었기 때문에 상태 키가 Redis 에서 삭제되어야 함 (\u201CDEL\u201D).
        Boolean exists = redisTemplate.hasKey(MatchingConstants.MATCH_STATUS_KEY + matchId);
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("클라이언트 마지막 수락 요청과 타임아웃 스케줄러 처리가 동시에 맞물릴 경우(경합) 충돌되지 않고 잘 처리된다.")
    public void testConcurrentAcceptanceAndTimeout() throws Exception {
        // given
        String matchId = UUID.randomUUID().toString();
        String userId1 = "userA";
        String userId2 = "userB";
        String queueKey = "queue:testRoom";

        List<MatchParticipant> participants = List.of(
                MatchParticipant.builder().userId(userId1).score(100.0).nickname("UserA").build(),
                MatchParticipant.builder().userId(userId2).score(100.0).nickname("UserB").build()
        );

        MatchStatusInfo statusInfo = MatchStatusInfo.builder()
                .groupSize("2")
                .acceptCount(0)
                .participants(participants)
                .queueKey(queueKey)
                .acceptedUsers(new HashMap<>())
                .build();

        matchStatusService.createMatchStatus(matchId, statusInfo, 10);
        matchingService.acceptMatch(userId1, matchId); // UserA 는 일단 수락 완료

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        // 테스트를 위해 private 타임아웃 메서드를 Reflection 으로 접근
        Method processMatchTimeoutMethod = MatchingEngineService.class.getDeclaredMethod("processMatchTimeout", String.class);
        processMatchTimeoutMethod.setAccessible(true);

        // when
        // Thread 1: UserB 마지막 수락 클릭
        executorService.submit(() -> {
            try {
                matchingService.acceptMatch(userId2, matchId);
            } finally {
                latch.countDown();
            }
        });

        // Thread 2: 백그라운드 스케줄러 타임아웃 강제 발동
        executorService.submit(() -> {
            try {
                processMatchTimeoutMethod.invoke(matchingEngineService, matchId);
            } catch (Exception e) {
                // Ignore Reflection Exceptions for brevity
            } finally {
                latch.countDown();
            }
        });

        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        // then
        // 이 경합의 승자가 누구든 (수락이 먼저 들어가 COMPLETED 가 되든, 삭제/반환되어 Timeout 이 되든)
        // Redis 에서 해당 매칭 키는 "잔여물 없이 삭제" (DEL) 되어 무결성을 보장되어야 한다.
        // 그리고 내부적으로 에러가 발생해서는 안 된다.
        Boolean exists = redisTemplate.hasKey(MatchingConstants.MATCH_STATUS_KEY + matchId);
        assertThat(exists).isFalse();
    }
}
