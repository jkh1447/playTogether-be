package com.jkh1447.MyProject.dto.matching;

import com.jkh1447.MyProject.domain.matching.exception.InvalidStringFormatException;
import com.jkh1447.MyProject.domain.matching.exception.MatchingErrorCode;
import lombok.Builder;
import java.util.Map;

@Builder
public record MatchParticipant(String userId, Double score, String nickname, String infos) {

    // "userId:score:nickname"

    public String toRedisFormat() {
        return String.format("%s|%s|%s|%s", userId, score, nickname, infos);
    }

    public static MatchParticipant fromRedisFormat(String redisFormat) {
        String[] parts = redisFormat.split("\\|", 4);
        if (parts.length < 4) {
            throw new InvalidStringFormatException(
                    MatchingErrorCode.INVALID_PARTICIPANT_STRING_FORMAT);
        }
        return MatchParticipant.builder().userId(parts[0]).score(Double.parseDouble(parts[1]))
                .nickname(parts[2]).infos(parts[3]).build();
    }
}
