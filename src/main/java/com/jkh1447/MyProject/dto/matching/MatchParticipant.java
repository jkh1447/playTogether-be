package com.jkh1447.MyProject.dto.matching;

import com.jkh1447.MyProject.domain.matching.exception.InvalidStringFormatException;
import com.jkh1447.MyProject.domain.matching.exception.MatchingErrorCode;
import lombok.Builder;
import java.util.Map;

@Builder
public record MatchParticipant(String userId, Double score, String nickname) {

    // "userId:score:nickname"

    public String toRedisFormat() {
        return String.format("%s:%s:%s", userId, score, nickname);
    }

    public static MatchParticipant fromRedisFormat(String redisFormat) {
        String[] parts = redisFormat.split(":", 3);
        if (parts.length < 3) {
            throw new InvalidStringFormatException(
                    MatchingErrorCode.INVALID_PARTICIPANT_STRING_FORMAT);
        }
        return MatchParticipant.builder().userId(parts[0]).score(Double.parseDouble(parts[1]))
                .nickname(parts[2]).build();
    }
}
