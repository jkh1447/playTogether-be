package com.jkh1447.MyProject.service.matching;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import com.jkh1447.MyProject.dto.matching.MatchingRequest;

@Component
public class MatchingKeyGenerator {

    private static final String PREFIX = "match";

    public String generateKey(MatchingRequest matchingRequest) {

        String groupSize = matchingRequest.filters().getOrDefault(MatchingConstants.MATCH_GROUP_SIZE, "3");

        String filterPart = matchingRequest.filters().entrySet().stream()
                .filter(entry -> !entry.getKey().equals(MatchingConstants.MATCH_GROUP_SIZE))
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        return String.format("%s:%s:groupSize=%s:%s", PREFIX, matchingRequest.gameName(), groupSize,
                filterPart);
    }
}
