package com.jkh1447.MyProject.service.matching.strategy.Valorant;

import java.util.Map;

public class ValorantConstants {
  public static final String GAME_ID = "Valorant";
  public static final String GROUP_SIZE_ANY = "상관없음";
  public static final String GAME_MODE = "mode";
  public static final int ANY_QUEUE_DEFAULT_GROUP_SIZE = 5;

  public static final String QUEUE_USER_INFO_MY_RANK = "myRank";
  public static final String QUEUE_USER_INFO_RANK_RANGE = "rankRange";
  // public static final String QUEUE_USER_INFO_POSITION = "position";

  public static final String POSITION_ANY = "상관없음";

  public static final String GAME_MODE_RANK = "경쟁전";
  public static final String GAME_MODE_NORMAL = "일반전";

  public static final Map<String, String> RANK_LEVEL = Map.of("아이언", "1", "브론즈", "2", "실버", "3",
      "골드", "4", "플래티넘", "5", "다이아몬드", "6", "초월자", "7", "레디언트", "8");
}
