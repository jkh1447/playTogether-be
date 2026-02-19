package com.jkh1447.MyProject.dto.matching;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import lombok.Builder;

/*
 * Redis Hash 구조 
 * 1. Match Status // 큐가 잡혔을 때 그 매치의 상태들을 저장
 * Key: {matchId}
 * Value: {groupSize:int, acceptCount:int, participants:String, queueKey:String}
 */

@Builder
public record MatchStatusInfo(
    int groupSize, 
    int acceptCount, 
    List<MatchParticipant> participants,
    String queueKey,
    Map<String, String> acceptedUsers) {

    public boolean isAllAccepted() {
        return this.acceptCount >= this.groupSize;
    }

    public boolean isAcceptedUser(String userId) {
        return this.acceptedUsers.containsKey(MatchingConstants.MATCH_ACCEPTED_PREFIX + userId);
    }

    public List<String> getParticipantIds() {
        return this.participants.stream().map(MatchParticipant::userId).toList();
    }

    public String participantsToRedisFormat() {
        return this.participants.stream().map(MatchParticipant::toRedisFormat).collect(Collectors.joining(","));
    }
}
