package com.jkh1447.MyProject.dto.matching;

import com.jkh1447.MyProject.domain.matching.exception.InvalidStringFormatException;
import com.jkh1447.MyProject.domain.matching.exception.MatchingErrorCode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class QueueInfo {
    private String gameName;
    private String groupSize;
    private String mode;

    // "match:gameName:groupSize=X:mode"

    public static QueueInfo fromQueueKey(String queueKey) {
        try {
            String[] parts = queueKey.split(":");
    
            String gameName = parts[1];
            String groupSize = parts[2].split("=")[1];
            String mode = parts[3];
            
            return QueueInfo.builder()
                    .gameName(gameName)
                    .groupSize(groupSize)
                    .mode(mode)
                    .build();
        } catch (Exception e) { 
            throw new InvalidStringFormatException(MatchingErrorCode.INVALID_QUEUE_KEY_FORMAT);
        }
    }
    
}
