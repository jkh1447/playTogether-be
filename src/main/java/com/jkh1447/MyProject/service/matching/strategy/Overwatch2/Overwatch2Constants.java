package com.jkh1447.MyProject.service.matching.strategy.Overwatch2;

import java.util.Map;

public class Overwatch2Constants {
  public static final String GAME_ID = "Overwatch2";

  public static final String GAME_MODE = "mode";

  public static final String QUEUE_USER_INFO_MY_RANK = "myRank";
  public static final String QUEUE_USER_INFO_RANK_RANGE = "rankRange";
  public static final String QUEUE_USER_INFO_POSITION = "position";

  public static final String GAME_MODE_RANK = "경쟁전";
  public static final String GAME_MODE_NORMAL = "일반전";

  public static final String POSITION_TANK = "탱커";
  public static final String POSITION_DAMAGE = "딜러";
  public static final String POSITION_SUPPORT = "지원";
  public static final String POSITION_ANY = "상관없음";

  public static final String GROUP_SIZE_ANY = "상관없음";
  public static final int ANY_QUEUE_DEFAULT_GROUP_SIZE = 5;

  public static final Map<String, String> RANK_LEVEL = Map.of("브론즈", "1", "실버", "2", "골드", "3",
      "플래티넘", "4", "다이아몬드", "5", "마스터", "6", "그랜드마스터", "7", "챔피언", "8");
}
