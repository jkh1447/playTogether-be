package com.jkh1447.MyProject.dto.matching;

import com.jkh1447.MyProject.domain.matching.exception.InvalidStringFormatException;
import com.jkh1447.MyProject.domain.matching.exception.MatchingErrorCode;
import lombok.Builder;

@Builder
public record QueueInfo(String gameName, int groupSize, String filterPart) {

    // "match:gameName:groupSize=X:tier=gold&mic=true&..." 형식

    public static QueueInfo fromQueueKey(String queueKey) {
        try {
            String[] parts = queueKey.split(":");
    
            String gameName = parts[1];
            String sizeValue = parts[2].split("=")[1];
            int groupSize = Integer.parseInt(sizeValue);
            String filterPart = parts[3];
            
            return QueueInfo.builder()
                    .gameName(gameName)
                    .groupSize(groupSize)
                    .filterPart(filterPart)
                    .build();
        } catch (Exception e) { 
            throw new InvalidStringFormatException(MatchingErrorCode.INVALID_QUEUE_KEY_FORMAT);
        }
    }
    
}
