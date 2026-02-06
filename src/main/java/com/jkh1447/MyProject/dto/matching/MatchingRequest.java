package com.jkh1447.MyProject.dto.matching;

import java.util.Map;

public record MatchingRequest(
    String gameName,
    Map<String, String> filters
) {
}
