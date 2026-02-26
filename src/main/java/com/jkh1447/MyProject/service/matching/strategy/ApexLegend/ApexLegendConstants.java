package com.jkh1447.MyProject.service.matching.strategy.ApexLegend;

import java.util.Map;

public class ApexLegendConstants {
  public static final String GAME_ID = "ApexLegend";

  public static final String GAME_MODE_RANK = "랭크";
  public static final String GAME_MODE_NORMAL = "일반전";

  public static final String GAME_MODE = "mode";
  public static final String QUEUE_USER_INFO_MY_RANK = "myRank";
  public static final String QUEUE_USER_INFO_RANK_RANGE = "rankRange";

  public static final String POSITION_ANY = "상관없음";
  public static final String GROUP_SIZE_ANY = "상관없음";
  public static final int ANY_QUEUE_DEFAULT_GROUP_SIZE = 3;

  public static final Map<String, String> RANK_LEVEL = Map.of("브론즈", "1", "실버", "2", "골드", "3",
    "플래티넘", "4", "다이아몬드", "5", "프레데터", "6");
}
