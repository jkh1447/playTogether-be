import http from "k6/http";
import ws from "k6/ws";
import { check, sleep } from "k6";
import encoding from "k6/encoding";
import { Trend } from "k6/metrics";

export const chatMessageLatency = new Trend("chat_message_latency", true);

export const options = {
  scenarios: {
    matching_and_chat: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        { duration: '30s', target: 50 },  // 30초 동안 가상 유저(VUser)를 0명에서 50명까지 서서히 증가 (Ramp-up)
        { duration: '1m', target: 50 },   // 1분 동안 가상 유저 50명을 일정하게 유지하며 부하 지속
        { duration: '30s', target: 100 }, // 30초 동안 가상 유저를 50명에서 100명으로 증가 (한계 도전)
        { duration: '1m', target: 100 },  // 1분 동안 가상 유저 100명 유지
        { duration: '30s', target: 0 },   // 30초 동안 가상 유저를 0명으로 줄이며 다운 (Ramp-down)
      ],
    },
  },
};

const BASE_URL = "https://da-gachi.com";
const WS_URL = "wss://da-gachi.com/ws-matching";

function parseJwt(token) {
  const parts = token.split(".");
  return JSON.parse(encoding.b64decode(parts[1], "rawurl", "s"));
}

export default function () {
  // 1. 게스트 로그인
  const loginRes = http.post(`${BASE_URL}/api/auth/guest`);
  check(loginRes, { "로그인 성공": (r) => r.status === 200 });

  const jar = http.cookieJar();
  const cookies = jar.cookiesForURL(BASE_URL);
  const accessToken = cookies.accessToken ? cookies.accessToken[0] : null;

  if (!accessToken) return;

  const payload = parseJwt(accessToken);
  const userId = payload.sub;
  const nickname = "게스트_" + userId.split("_")[1].substring(0, 4);

  // 2. 매칭 큐 참가
  const joinPayload = JSON.stringify({
    gameName: "LoL",
    filters: {
      mode: "랭크",
      groupSize: "2",
      myRank: "실버",
      rankRange: "아이언~챌린저",
      position: "상관없음",
    },
  });

  const joinRes = http.post(`${BASE_URL}/api/matching/join`, joinPayload, {
    headers: { "Content-Type": "application/json" },
  });
  check(joinRes, { "매칭 큐 참가 성공": (r) => r.status === 200 });

  // 3. 웹소켓 연결 및 STOMP 통신
  const wsRes = ws.connect(
    WS_URL,
    { headers: { Cookie: `accessToken=${accessToken}` } },
    function (socket) {
      let chatIntervalId = null;
      let connected = false;

      socket.on("open", function () {
        // heartbeat 비활성화 (0,0)
        socket.send("CONNECT\naccept-version:1.1,1.2\nheart-beat:0,0\n\n\0");
      });

      socket.on("message", function (msg) {
        // heartbeat 프레임 (빈 줄) 무시
        if (!msg || msg.trim() === "" || msg === "\n") {
          return;
        }

        // STOMP ERROR 프레임 처리
        if (msg.startsWith("ERROR")) {
          console.error(`[VU ${__VU}] STOMP ERROR: ${msg.substring(0, 200)}`);
          return;
        }

        if (msg.startsWith("CONNECTED")) {
          connected = true;
          socket.send(
            "SUBSCRIBE\nid:sub-0\ndestination:/user/queue/match-found\n\n\0",
          );
          socket.send(
            "SUBSCRIBE\nid:sub-1\ndestination:/user/queue/move-room\n\n\0",
          );
          console.log(`[VU ${__VU}] 웹소켓 연결 및 구독 완료 (${nickname})`);
        } else if (msg.startsWith("MESSAGE")) {
          const parts = msg.split("\n\n");
          if (parts.length < 2) return;

          const bodyStr = parts.slice(1).join("\n\n").replace(/\0/g, "");
          const headersStr = parts[0];

          let data;
          try {
            data = JSON.parse(bodyStr);
          } catch (e) {
            console.warn(
              `[VU ${__VU}] JSON 파싱 실패: ${bodyStr.substring(0, 100)}`,
            );
            return;
          }

          // A. 매칭 발견 시
          if (headersStr.includes("/queue/match-found")) {
            const matchId = data.matchId;
            console.log(`[VU ${__VU}] 매칭 발견! matchId: ${matchId}`);

            socket.setTimeout(() => {
              const acceptRes = http.post(
                `${BASE_URL}/api/matching/accept`,
                JSON.stringify({ matchId: matchId }),
                {
                  headers: {
                    "Content-Type": "application/json",
                    Cookie: `accessToken=${accessToken}`,
                  },
                },
              );
              check(acceptRes, { "매칭 수락 성공": (r) => r.status === 200 });
            }, 500);
          }

          // B. 매칭 성사 및 방 이동
          else if (headersStr.includes("/queue/move-room")) {
            const roomId = data.roomId;
            console.log(`[VU ${__VU}] 매칭 성사! 방 이동... roomId: ${roomId}`);

            socket.setTimeout(() => {
              socket.send(
                `SUBSCRIBE\nid:sub-chat-${roomId}\ndestination:/topic/room/${roomId}\nroomId:${roomId}\n\n\0`,
              );
              console.log(`[VU ${__VU}] 채팅방 입장 및 전송 시작`);

              let msgCount = 0;
              chatIntervalId = socket.setInterval(() => {
                if (msgCount >= 30) {
                  if (chatIntervalId) {
                    socket.clearInterval(chatIntervalId);
                    chatIntervalId = null;
                  }
                  console.log(`[VU ${__VU}] 메시지 전송 완료, 소켓 종료 중...`);
                  socket.send(`UNSUBSCRIBE\nid:sub-chat-${roomId}\n\n\0`);
                  socket.setTimeout(() => {
                    socket.close();
                  }, 2000);
                  return;
                }

                const sentTime = Date.now();
                const chatMsg = {
                  roomId: roomId,
                  type: "TALK",
                  senderId: userId,
                  senderNickname: nickname,
                  content: `[ts:${sentTime}] test ${msgCount}`,
                };

                socket.send(
                  `SEND\ndestination:/app/chat/${roomId}\ncontent-type:application/json\n\n${JSON.stringify(chatMsg)}\0`,
                );
                msgCount++;
                console.log(`[VU ${__VU}] 메시지 전송: ${msgCount}/30`);
              }, 500);
            }, 1000);
          }

          // C. 채팅 수신
          else if (headersStr.includes("/topic/room/")) {
            if (data.type === "TALK" && data.senderId === userId) {
              const tsMatch = data.content.match(/\[ts:(\d+)\]/);
              if (tsMatch) {
                const sentTime = parseInt(tsMatch[1], 10);
                chatMessageLatency.add(Date.now() - sentTime);
              }
            }
          }
        }
      });

      socket.on("error", function (e) {
        if (chatIntervalId) {
          socket.clearInterval(chatIntervalId);
          chatIntervalId = null;
        }
        if (e.error() !== "websocket: close sent") {
          console.error(`[VU ${__VU}] 소켓 에러: ${e.error()}`);
        }
      });

      socket.on("close", function () {
        if (chatIntervalId) {
          socket.clearInterval(chatIntervalId);
          chatIntervalId = null;
        }
      });

      // 전체 타임아웃: 90초
      socket.setTimeout(() => {
        socket.close();
      }, 90000);
    },
  );

  check(wsRes, { "웹소켓 정상 종료": (r) => r && r.status === 101 });
}
