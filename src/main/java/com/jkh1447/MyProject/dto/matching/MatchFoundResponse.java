package com.jkh1447.MyProject.dto.matching;

import lombok.Builder;

@Builder
public record MatchFoundResponse(
    String matchId,     // 수락/거절 시 서버에 다시 보내줄 ID
    long timeoutSeconds // 수락 대기 시간 (예: 15초)
) {
}