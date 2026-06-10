import http from "k6/http";
import ws from "k6/ws";
import { check } from "k6";
import encoding from "k6/encoding";
import { Trend, Counter, Rate } from "k6/metrics";

// ════════════════════════════════════════════════════════════════
//  커스텀 메트릭 - 병목 구간별 측정
// ════════════════════════════════════════════════════════════════

// ── 구간별 지연 (Trend: avg / p50 / p90 / p95 / p99 / max 자동 출력) ──
// 1) 로그인 API 응답 시간
export const loginDuration       = new Trend("dur_login",        true);
// 2) WebSocket TCP+TLS 핸드셰이크 시간 (ws.connect 호출 ~ CONNECTED 수신)
export const wsHandshakeDuration = new Trend("dur_ws_handshake", true);
// 3) 매칭 큐 참가 API 응답 시간
export const joinDuration        = new Trend("dur_join",         true);
// 4) 큐 진입 ~ match-found 수신 (매칭 엔진 처리 대기)
export const matchWaitDuration   = new Trend("dur_match_wait",   true);
// 5) 매칭 수락 API 응답 시간
export const acceptDuration      = new Trend("dur_accept",       true);
// 6) accept 완료 ~ move-room 수신 (모든 참가자 수락 집계 + 방 생성 시간)
export const moveRoomDelay       = new Trend("dur_move_room",    true);
// 7) 채팅 메시지 왕복 지연 (SEND → /topic/room echo 수신)
export const chatRttDuration     = new Trend("dur_chat_rtt",     true);

// ── 성공/실패율 (Rate) ──
export const loginSuccessRate    = new Rate("rate_login_success");
export const wsConnectRate       = new Rate("rate_ws_connect");
export const joinSuccessRate     = new Rate("rate_join_success");
export const acceptSuccessRate   = new Rate("rate_accept_success");
export const matchFoundRate      = new Rate("rate_match_found");   // 90s 내 매칭 발견 여부
export const moveRoomRate        = new Rate("rate_move_room");     // 매칭 수락 후 방 이동 성공 여부
export const chatCompleteRate    = new Rate("rate_chat_complete"); // 채팅 30건 전송 완료 여부

// ── 카운터 ──
export const matchTimeoutCount   = new Counter("count_match_timeout");  // 90s 내 매칭 못 찾은 VU 수
export const acceptFailCount     = new Counter("count_accept_fail");    // 수락 실패 횟수
export const stompErrorCount     = new Counter("count_stomp_error");    // STOMP ERROR 프레임 수
export const wsErrorCount        = new Counter("count_ws_error");       // WebSocket 에러 수

// ════════════════════════════════════════════════════════════════
//  부하 시나리오
// ════════════════════════════════════════════════════════════════
export const options = {
  scenarios: {
    matching_and_chat: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        { duration: "1m", target: 25 }, // 1분 동안 100명까지 증가
    { duration: "1m", target: 50 }, // 3분 유지
    { duration: "30s", target: 0 },
      ],
    },
  },
  thresholds: {
    // 로그인 p95 < 500ms
    "dur_login":           ["p(95)<500"],
    // WS 핸드셰이크 p95 < 1000ms
    "dur_ws_handshake":    ["p(95)<1000"],
    // 큐 참가 p95 < 500ms
    "dur_join":            ["p(95)<500"],
    // 매칭 수락 p95 < 1000ms
    "dur_accept":          ["p(95)<1000"],
    // 채팅 왕복 p95 < 2000ms
    "dur_chat_rtt":        ["p(95)<2000"],
    // 로그인 성공률 99% 이상
    "rate_login_success":  ["rate>0.99"],
    // WS 연결 성공률 99% 이상
    "rate_ws_connect":     ["rate>0.99"],
    // 큐 참가 성공률 99% 이상
    "rate_join_success":   ["rate>0.99"],
    // 매칭 수락 성공률 95% 이상
    "rate_accept_success": ["rate>0.95"],
  },
};

// ════════════════════════════════════════════════════════════════
//  상수
// ════════════════════════════════════════════════════════════════
const BASE_URL = "https://da-gachi.com";
const WS_URL   = "wss://da-gachi.com/ws-matching";

// VU → 3개 큐 균등 분산 (큐 키: queue:LoL:groupSize=5:{mode})
const QUEUE_PROFILES = [
  { mode: "랭크",     groupSize: "5", myRank: "실버", rankRange: "아이언~챌린저", position: "상관없음" },
  { mode: "자유랭크", groupSize: "5", myRank: "골드",  rankRange: "아이언~챌린저", position: "상관없음" },
  { mode: "일반게임", groupSize: "5", position: "상관없음" },
];

// ════════════════════════════════════════════════════════════════
//  유틸
// ════════════════════════════════════════════════════════════════
function parseJwt(token) {
  const parts = token.split(".");
  try {
    return JSON.parse(encoding.b64decode(parts[1], "rawurl", "s"));
  } catch (_) {
    return null;
  }
}

// ════════════════════════════════════════════════════════════════
//  메인
// ════════════════════════════════════════════════════════════════
export default function () {

  // ── 1. 게스트 로그인 ──────────────────────────────────────────
  const loginStart = Date.now();
  const loginRes   = http.post(`${BASE_URL}/api/auth/guest`);
  loginDuration.add(Date.now() - loginStart);

  const loginOk = loginRes.status === 200;
  loginSuccessRate.add(loginOk ? 1 : 0);
  check(loginRes, { "로그인 성공": () => loginOk });

  if (!loginOk) {
    console.error(`[LOGIN FAIL] status=${loginRes.status}`);
    return;
  }

  // 쿠키 추출
  const jar    = http.cookieJar();
  const cookies = jar.cookiesForURL(BASE_URL);
  const accessToken = cookies.accessToken ? cookies.accessToken[0] : null;
  if (!accessToken) {
    console.error(`[TOKEN MISSING] VU=${__VU}`);
    return;
  }

  const payload = parseJwt(accessToken);
  if (!payload || !payload.sub) {
    console.error(`[JWT PARSE FAIL] VU=${__VU}`);
    return;
  }
  const userId   = payload.sub;
  const nickname = "게스트_" + (userId.split("_")[1] || userId).substring(0, 4);

  // 큐 프로파일 선택 (VU 번호 기반 라운드로빈)
  const profile     = QUEUE_PROFILES[(__VU - 1) % QUEUE_PROFILES.length];
  const joinPayload = JSON.stringify({ gameName: "LoL", filters: profile });

  // ── 2. WebSocket 연결 → 구독 → 큐 참가 ──────────────────────
  const wsStart = Date.now();
  const wsRes   = ws.connect(
    WS_URL,
    { headers: { Cookie: `accessToken=${accessToken}` } },
    function (socket) {

      // ── 상태 변수 ─────────────────────────────────────────────
      let queueJoinTime    = null;  // 큐 진입 시각
      let acceptSentTime   = null;  // accept 전송 완료 시각
      let matchFoundOnce   = false; // match-found 중복 방지
      let acceptSentOnce   = false; // accept 중복 방지
      let matchFound       = false; // 90s 타임아웃 판단용
      let movedToRoom      = false; // move-room 수신 여부
      let chatCompleted    = false; // 채팅 완료 여부
      let chatIntervalId   = null;

      // ── open: STOMP CONNECT ───────────────────────────────────
      socket.on("open", function () {
        socket.send("CONNECT\naccept-version:1.1,1.2\nheart-beat:0,0\n\n\0");
      });

      // ── message ───────────────────────────────────────────────
      socket.on("message", function (msg) {
        if (!msg || msg.trim() === "" || msg === "\n") return;

        // STOMP ERROR
        if (msg.startsWith("ERROR")) {
          stompErrorCount.add(1);
          console.error(`[STOMP ERROR] VU=${__VU} ${msg.substring(0, 200)}`);
          socket.close();
          return;
        }

        // ── STOMP CONNECTED ──────────────────────────────────────
        if (msg.startsWith("CONNECTED")) {
          const handshakeMs = Date.now() - wsStart;
          wsHandshakeDuration.add(handshakeMs);
          wsConnectRate.add(1);
          console.log(`[WS CONNECTED] VU=${__VU} mode=${profile.mode} handshake=${handshakeMs}ms`);

          // 구독 (매칭 알림 먼저)
          socket.send("SUBSCRIBE\nid:sub-0\ndestination:/user/queue/match-found\n\n\0");
          socket.send("SUBSCRIBE\nid:sub-1\ndestination:/user/queue/move-room\n\n\0");

          // 큐 참가 (/api/matching/join)
          const joinStart = Date.now();
          const joinRes   = http.post(`${BASE_URL}/api/matching/join`, joinPayload, {
            headers: { "Content-Type": "application/json" },
          });
          const joinMs = Date.now() - joinStart;
          joinDuration.add(joinMs);

          const joinOk = joinRes.status === 200;
          joinSuccessRate.add(joinOk ? 1 : 0);
          check(joinRes, { "매칭 큐 참가 성공": () => joinOk });

          if (!joinOk) {
            console.error(`[JOIN FAIL] VU=${__VU} status=${joinRes.status} dur=${joinMs}ms`);
            return;
          }

          queueJoinTime = Date.now();
          console.log(`[JOIN OK] VU=${__VU} mode=${profile.mode} dur=${joinMs}ms`);
          return;
        }

        // ── STOMP MESSAGE ────────────────────────────────────────
        if (!msg.startsWith("MESSAGE")) return;

        const splitIdx = msg.indexOf("\n\n");
        if (splitIdx === -1) return;
        const headersStr = msg.substring(0, splitIdx);
        const bodyStr    = msg.substring(splitIdx + 2).replace(/\0/g, "").trim();
        if (!bodyStr) return;

        let data;
        try {
          data = JSON.parse(bodyStr);
        } catch (e) {
          console.warn(`[JSON FAIL] VU=${__VU} body=${bodyStr.substring(0, 80)}`);
          return;
        }

        // ── A. 매칭 발견 (/user/queue/match-found) ───────────────
        if (headersStr.includes("/queue/match-found")) {
          matchFound = true;
          matchFoundRate.add(1);

          // 매칭 대기 시간 (첫 번째 수신만 기록)
          if (!matchFoundOnce && queueJoinTime) {
            matchFoundOnce = true;
            const waitMs = Date.now() - queueJoinTime;
            matchWaitDuration.add(waitMs);
            console.log(`[MATCH FOUND] VU=${__VU} mode=${profile.mode} wait=${waitMs}ms`);
          }

          // 중복 수락 방지
          if (acceptSentOnce) return;
          acceptSentOnce = true;

          const matchId = data.matchId;

          // 수락 (500ms 후)
          socket.setTimeout(() => {
            const acceptStart = Date.now();
            const acceptRes   = http.post(
              `${BASE_URL}/api/matching/accept`,
              JSON.stringify({ matchId }),
              {
                headers: {
                  "Content-Type": "application/json",
                  Cookie: `accessToken=${accessToken}`,
                },
              },
            );
            const acceptMs = Date.now() - acceptStart;
            acceptDuration.add(acceptMs);

            const acceptOk = acceptRes.status === 200;
            acceptSuccessRate.add(acceptOk ? 1 : 0);
            check(acceptRes, { "매칭 수락 성공": () => acceptOk });

            if (!acceptOk) {
              acceptFailCount.add(1);
              console.error(`[ACCEPT FAIL] VU=${__VU} status=${acceptRes.status} dur=${acceptMs}ms matchId=${matchId}`);
            } else {
              acceptSentTime = Date.now(); // accept 완료 시각 기록
              console.log(`[ACCEPT OK] VU=${__VU} matchId=${matchId} dur=${acceptMs}ms`);
            }
          }, 500);
          return;
        }

        // ── B. 매칭 성사 / 방 이동 (/user/queue/move-room) ───────
        if (headersStr.includes("/queue/move-room")) {
          movedToRoom = true;
          moveRoomRate.add(1);

          // accept 완료 ~ move-room 수신 지연 (방 생성 + 전원 수락 집계 시간)
          if (acceptSentTime) {
            const moveDelayMs = Date.now() - acceptSentTime;
            moveRoomDelay.add(moveDelayMs);
            console.log(`[MOVE ROOM] VU=${__VU} roomId=${data.roomId} acceptToMove=${moveDelayMs}ms`);
          }

          const roomId = data.roomId;

          socket.setTimeout(() => {
            // 채팅방 구독
            socket.send(
              `SUBSCRIBE\nid:sub-chat-${roomId}\ndestination:/topic/room/${roomId}\nroomId:${roomId}\n\n\0`,
            );

            let msgCount = 0;
            chatIntervalId = socket.setInterval(() => {
              if (msgCount >= 30) {
                if (chatIntervalId) {
                  socket.clearInterval(chatIntervalId);
                  chatIntervalId = null;
                }
                chatCompleted = true;
                chatCompleteRate.add(1);
                console.log(`[CHAT DONE] VU=${__VU} roomId=${roomId}`);
                socket.send(`UNSUBSCRIBE\nid:sub-chat-${roomId}\n\n\0`);
                socket.setTimeout(() => socket.close(), 2000);
                return;
              }

              const sentTime = Date.now();
              socket.send(
                `SEND\ndestination:/app/chat/${roomId}\ncontent-type:application/json\n\n${JSON.stringify({
                  roomId,
                  type: "TALK",
                  senderId: userId,
                  senderNickname: nickname,
                  content: `[ts:${sentTime}] test ${msgCount}`,
                })}\0`,
              );
              msgCount++;
            }, 500);
          }, 1000);
          return;
        }

        // ── C. 채팅 수신 → 왕복 지연 측정 (/topic/room/:id) ──────
        if (headersStr.includes("/topic/room/")) {
          if (data.type === "TALK" && data.senderId === userId) {
            const tsMatch = data.content && data.content.match(/\[ts:(\d+)\]/);
            if (tsMatch) {
              const rttMs = Date.now() - parseInt(tsMatch[1], 10);
              chatRttDuration.add(rttMs);
            }
          }
        }
      });

      // ── error ─────────────────────────────────────────────────
      socket.on("error", function (e) {
        if (chatIntervalId) {
          socket.clearInterval(chatIntervalId);
          chatIntervalId = null;
        }
        if (e.error() !== "websocket: close sent") {
          wsErrorCount.add(1);
          console.error(`[WS ERROR] VU=${__VU} ${e.error()}`);
        }
      });

      // ── close: 미완료 구간 실패 기록 ──────────────────────────
      socket.on("close", function () {
        if (chatIntervalId) {
          socket.clearInterval(chatIntervalId);
          chatIntervalId = null;
        }
        // 매칭을 못 찾은 경우 실패로 기록
        if (!matchFound) {
          matchFoundRate.add(0);
        }
        // 방으로 이동 못 한 경우 실패로 기록
        if (matchFound && !movedToRoom) {
          moveRoomRate.add(0);
        }
        // 채팅 미완료 기록
        if (movedToRoom && !chatCompleted) {
          chatCompleteRate.add(0);
        }
      });

      // ── 전체 타임아웃 (90초) ─────────────────────────────────
      socket.setTimeout(() => {
        matchTimeoutCount.add(1);
        console.warn(`[TIMEOUT] VU=${__VU} mode=${profile.mode} matchFound=${matchFound}`);
        socket.close();
      }, 90000);
    },
  );

  // WS 연결 자체 실패 감지 (핸드셰이크 전 실패)
  if (!wsRes || wsRes.status !== 101) {
    wsConnectRate.add(0);
    console.error(`[WS CONNECT FAIL] VU=${__VU} status=${wsRes ? wsRes.status : "null"}`);
  }
}
