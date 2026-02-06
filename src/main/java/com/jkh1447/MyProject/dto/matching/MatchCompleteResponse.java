package com.jkh1447.MyProject.dto.matching;

import lombok.Builder;

@Builder
public record MatchCompleteResponse(
    String roomId,
    String status
) {
}
