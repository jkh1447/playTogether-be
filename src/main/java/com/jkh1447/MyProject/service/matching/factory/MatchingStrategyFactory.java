package com.jkh1447.MyProject.service.matching.factory;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

import com.jkh1447.MyProject.service.matching.strategy.MatchStrategy;

public class MatchingStrategyFactory {

  private final Map<String, MatchStrategy> strategies;

  public MatchingStrategyFactory(List<MatchStrategy> strategyList) {
    strategies = strategyList.stream().collect(Collectors.toMap(s -> s.getGameName(), s -> s));
  }

  public MatchStrategy getStrategy(String gameName) {
    return strategies.get(gameName);
  }
  
}
