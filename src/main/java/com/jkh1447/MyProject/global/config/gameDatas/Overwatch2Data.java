package com.jkh1447.MyProject.global.config.gameDatas;

import java.util.List;

import org.springframework.stereotype.Component;

import com.jkh1447.MyProject.domain.gameInfo.GameInfo;
import com.jkh1447.MyProject.dto.gameInfo.GameConditionDto;
import com.jkh1447.MyProject.service.matching.strategy.Overwatch2.Overwatch2Constants;

@Component
public class Overwatch2Data implements GameDataComponent {

  @Override
  public String getGameName() {
    return Overwatch2Constants.GAME_ID;
  }

  @Override
  public GameInfo getGameInfo() {

    List<GameConditionDto> overwatch2Conditions = List.of(
      GameConditionDto.builder().id("mode").label("게임 모드").type("select")
        .options(List.of("일반전", "경쟁전")).defaultValue("경쟁전").build(),
      GameConditionDto.builder().id("groupSize").label("인원").type("select")
        .options(List.of("상관없음", "2", "3", "4", "5")).defaultValue("2").build(),
      GameConditionDto.builder().id("myRank").label("내 랭크").type("select")
        .options(List.of("브론즈", "실버", "골드", "플래티넘", "다이아몬드", "마스터", "그랜드마스터", "챔피언"))
        .defaultValue("골드").dependsOn("mode").dependsOnValues(List.of("경쟁전")).build(),
      GameConditionDto.builder().id("rankRange").label("랭크 범위").type("threshold")
        .thresholdOptions(List.of("브론즈", "실버", "골드", "플래티넘", "다이아몬드", "마스터", "그랜드마스터", "챔피언"))
        .defaultValue("브론즈~챔피언").dependsOn("mode").dependsOnValues(List.of("경쟁전")).build(),
      GameConditionDto.builder().id("position").label("포지션").type("select")
        .options(List.of("상관없음", "탱커", "딜러", "지원")).defaultValue("상관없음").
        dependsOn("mode").dependsOnValues(List.of("경쟁전")).build()
    );

    return GameInfo.builder().id(Overwatch2Constants.GAME_ID).name("Overwatch2")
        .description("5v5 캐릭터 기반 전술 슈팅 게임").playerCount("5명")
        .conditions(overwatch2Conditions)
        .build();
  }
}
