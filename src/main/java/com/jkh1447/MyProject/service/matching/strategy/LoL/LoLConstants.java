package com.jkh1447.MyProject.service.matching.strategy.LoL;

import java.util.Map;

public class LoLConstants {
  public static final String GAME_ID = "LoL";
  public static final String GAME_MODE = "mode";
  public static final String GAME_MODE_RANK = "랭크";
  public static final String GAME_MODE_ARAMCHAOS = "증바람";
  public static final String GAME_MODE_FLEX = "자유랭크";
  public static final String GAME_MODE_NORMAL = "일반게임";
  public static final String GAME_MODE_ARAM = "칼바람 나락";

  public static final String QUEUE_USER_INFO_MY_RANK = "myRank";
  public static final String QUEUE_USER_INFO_RANK_RANGE = "rankRange";
  public static final String QUEUE_USER_INFO_POSITION = "position";

  public static final String POSITION_ANY = "상관없음";
  public static final String GROUP_SIZE_ANY = "상관없음";
  public static final int ANY_QUEUE_DEFAULT_GROUP_SIZE = 5;

  public static final Map<String, String> RANK_LEVEL = Map.of("아이언", "1", "브론즈", "2", "실버", "3",
      "골드", "4", "플래티넘", "5", "에메랄드", "6", "다이아몬드", "7", "마스터", "8", "그랜드마스터", "9", "챌린저", "10");
}
