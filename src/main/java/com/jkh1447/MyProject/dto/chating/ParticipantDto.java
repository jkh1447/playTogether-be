package com.jkh1447.MyProject.dto.chating;

import java.util.List;
import lombok.Builder;

@Builder
public record ParticipantDto(
    String type,
    List<ParticipantInfo> participants) {
    
    @Builder
    public record ParticipantInfo(
        String userId,
        String nickname) {
    }

    public static ParticipantDto createParticipantDto(List<ParticipantInfo> participants) {
        return ParticipantDto.builder()
                .type("PARTICIPANT")
                .participants(participants)
                .build();
    }
}
