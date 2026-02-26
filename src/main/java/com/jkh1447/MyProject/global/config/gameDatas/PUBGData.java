package com.jkh1447.MyProject.global.config.gameDatas;

import org.springframework.stereotype.Component;

import java.util.List;

import com.jkh1447.MyProject.domain.gameInfo.GameInfo;
import com.jkh1447.MyProject.dto.gameInfo.GameConditionDto;
import com.jkh1447.MyProject.service.matching.strategy.PUBG.PUBGConstants;

@Component
public class PUBGData implements GameDataComponent {

  @Override
  public String getGameName() {
    return PUBGConstants.GAME_ID;
  }

  @Override
  public GameInfo getGameInfo() {

    List<GameConditionDto> pubgConditions = List.of(
        GameConditionDto.builder().id("mode").label("게임 모드").type("select")
            .options(List.of("일반전", "랭크")).defaultValue("랭크").build(),
        GameConditionDto.builder().id("groupSize").label("인원").type("select")
            .options(List.of("상관없음", "2", "3", "4")).defaultValue("2").build(),
        GameConditionDto.builder().id("myRank").label("내 랭크").type("select")
            .options(List.of("브론즈", "실버", "골드", "플래티넘", "크리스탈", "다이아몬드", "마스터", "서바이버"))
            .defaultValue("골드").dependsOn("mode").dependsOnValues(List.of("랭크")).build(),
        GameConditionDto.builder().id("rankRange").label("랭크 범위").type("threshold")
            .thresholdOptions(List.of("브론즈", "실버", "골드", "플래티넘", "크리스탈", "다이아몬드", "마스터", "서바이버"))
            .defaultValue("브론즈~서바이버").dependsOn("mode").dependsOnValues(List.of("랭크")).build(),
        GameConditionDto.builder().id("viewMode").label("시점").type("select")
            .options(List.of("FPP", "TPP")).defaultValue("TPP").dependsOn("mode")
            .dependsOnValues(List.of("랭크", "일반전")).build());


    return GameInfo.builder().id(PUBGConstants.GAME_ID).name("PUBG").description("배틀로얄 슈팅 게임")
        .playerCount("4명").conditions(pubgConditions).build();
  }
}
