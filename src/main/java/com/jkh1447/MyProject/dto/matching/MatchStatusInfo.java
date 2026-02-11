package com.jkh1447.MyProject.dto.matching;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;

@Builder
public record MatchStatusInfo(int groupSize, int acceptCount, List<MatchParticipant> participants,
        String queueKey) {

    public boolean isAllAccepted() {
        return this.acceptCount >= this.groupSize;
    }

    public List<String> getParticipantIds() {
        return this.participants.stream().map(MatchParticipant::userId).toList();
    }

    public String participantsToRedisFormat() {
        return this.participants.stream().map(MatchParticipant::toRedisFormat).collect(Collectors.joining(","));
    }
}
