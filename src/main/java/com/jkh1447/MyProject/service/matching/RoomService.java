package com.jkh1447.MyProject.service.matching;

import org.springframework.stereotype.Service;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import com.jkh1447.MyProject.dto.matching.MatchParticipant;
import com.jkh1447.MyProject.domain.chating.Room;
import com.jkh1447.MyProject.repository.room.RoomRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.List;
import java.util.Map;
import java.time.Duration;
import java.util.HashMap;
import java.util.ArrayList;
import com.jkh1447.MyProject.domain.chating.ChatingConstants;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    /*
     * Redis Hash 구조 

     * 1. Room Participants // 방에 누가 있는가? 
     * Key: room:participants:{roomId} 
     * Value: {userId:nickname, userId:nickname, ...}
     * 
     * 2. Room Status // 방 상태
     * Key: room:status:{roomId}
     * Value: {userId:status, userId:status, ...}
     */

    private final RedisTemplate<String, Object> redisTemplate;
    private final RoomRepository roomRepository;

    /*
     * roomStatus 생성 참가자들에게 방 이동 알림
     */

    public void createRoom(String roomId, List<MatchParticipant> participants) {
        String roomParticipantsKey = buildRoomParticipantsKey(roomId);
        String roomStatusKey = buildRoomStatusKey(roomId);

        Map<String, Object> roomParticipants = new HashMap<>();
        List<String> participantsList = new ArrayList<>();

        Map<String, Object> roomStatus = new HashMap<>();
        roomStatus.put(ChatingConstants.CHAT_PARTICIPANTS_COUNT, participants.size());

        for (MatchParticipant participant : participants) {
            roomParticipants.put(participant.userId(), participant.nickname());
            participantsList.add(participant.userId());

            roomStatus.put(participant.userId(), ChatingConstants.CHAT_STATUS_INVITED);
        }

        Room room = Room.builder().roomId(roomId).participants(participantsList).build();
        // 방정보는 참가자들을 저장하지만, 현재 채팅방 참가자는 구독시 추가
        roomRepository.save(room);

        redisTemplate.opsForHash().putAll(roomStatusKey, roomStatus);
        // redisTemplate.opsForHash().putAll(roomStatusKey, roomParticipants);
        // redisTemplate.expire(roomStatusKey,
        //         Duration.ofSeconds(MatchingConstants.ROOM_EXPIRE_SECONDS));
    }

    public void addParticipant(String roomId, String userId, String nickname) {
        String roomStatusKey = buildRoomParticipantsKey(roomId);
        redisTemplate.opsForHash().putIfAbsent(roomStatusKey, userId, nickname);
        redisTemplate.expire(roomStatusKey,
                Duration.ofSeconds(MatchingConstants.ROOM_EXPIRE_SECONDS));
    }

    private String buildRoomParticipantsKey(String roomId) {
        return MatchingConstants.ROOM_PARTICIPANTS_KEY + roomId;
    }

    private String buildRoomStatusKey(String roomId) {
        return MatchingConstants.ROOM_STATUS_KEY + roomId;
    }

    public Map<Object, Object> getRoomStatus(String roomId) {
        String roomStatusKey = buildRoomParticipantsKey(roomId);
        return redisTemplate.opsForHash().entries(roomStatusKey);
    }

    public void deleteRoom(String roomId) {
        String roomStatusKey = buildRoomParticipantsKey(roomId);
        Boolean isDeleted = redisTemplate.delete(roomStatusKey);

        if (Boolean.TRUE.equals(isDeleted)) {
            log.info("[방 삭제] roomId: {}", roomId);
        }
    }


}
