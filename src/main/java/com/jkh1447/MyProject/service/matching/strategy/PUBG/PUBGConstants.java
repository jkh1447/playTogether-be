package com.jkh1447.MyProject.service.matching.strategy.PUBG;

import java.util.Map;

public class PUBGConstants {
  public static final String GAME_ID = "PUBG";

  public static final String GAME_MODE = "mode";

  public static final String QUEUE_USER_INFO_MY_RANK = "myRank";
  public static final String QUEUE_USER_INFO_RANK_RANGE = "rankRange";
  public static final String QUEUE_USER_INFO_VIEW_MODE = "viewMode";

  public static final String GAME_MODE_RANK = "랭크";
  public static final String GAME_MODE_NORMAL = "일반전";

  public static final String VIEW_MODE_FPP = "FPP";
  public static final String VIEW_MODE_TPP = "TPP";

  public static final String GROUP_SIZE_ANY = "상관없음";
  public static final int ANY_QUEUE_DEFAULT_GROUP_SIZE = 4;

  public static final Map<String, String> RANK_LEVEL = Map.of("브론즈", "1", "실버", "2", "골드", "3",
      "플래티넘", "4", "크리스탈", "5", "다이아몬드", "6", "마스터", "7", "서바이버", "8");
}
