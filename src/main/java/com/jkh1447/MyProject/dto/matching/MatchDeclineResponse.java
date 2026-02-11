package com.jkh1447.MyProject.dto.matching;

import lombok.Builder;

@Builder
public record MatchDeclineResponse(String matchId, Status status) {
    
    public enum Status{
        REJECTED,
        CANCELLED
    }

}
