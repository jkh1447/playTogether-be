package com.jkh1447.MyProject.dto.chating;

import java.util.List;
import lombok.Builder;

@Builder
public record ParticipantsDto(String type, List<ParticipantInfo> participants) {

    @Builder
    public record ParticipantInfo(String userId, String nickname) {
    }

    public static ParticipantsDto createParticipantDto(List<ParticipantInfo> participants) {
        return ParticipantsDto.builder().type("PARTICIPANT").participants(participants).build();
    }
}
