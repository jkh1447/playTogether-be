package com.jkh1447.MyProject.dto.chating;

import lombok.Builder;

@Builder
public record ParticipantInfo(String userId, String nickname) {
    
}
