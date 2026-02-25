package com.jkh1447.MyProject.service.matching.strategy;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class MatchStrategyFactory {

  private final Map<String, MatchStrategy> strategies;

  public MatchStrategyFactory(List<MatchStrategy> strategyList) {
    strategies = strategyList.stream().collect(Collectors.toMap(s -> s.getGameId(), s -> s));
  }

  public MatchStrategy getStrategy(String gameName) {
    return strategies.get(gameName);
  }

}
