package com.jkh1447.MyProject.global.config.gameDatas;

import org.springframework.stereotype.Component;

import com.jkh1447.MyProject.domain.gameInfo.GameInfo;
import com.jkh1447.MyProject.dto.gameInfo.GameConditionDto;
import com.jkh1447.MyProject.service.matching.strategy.LoL.LoLConstants;

import java.util.List;

@Component
public class LoLData implements GameDataComponent {

  @Override
  public String getGameName() {
    return LoLConstants.GAME_ID;
  }

  @Override
  public GameInfo getGameInfo() {
    List<GameConditionDto> lolConditions = List.of(
        GameConditionDto.builder().id("mode").label("게임 모드").type("select")
            .options(List.of("랭크", "증바람", "자유랭크", "일반게임", "칼바람 나락")).defaultValue("랭크").build(),
        GameConditionDto.builder().id("groupSize").label("인원").type("select")
            .options(List.of("상관없음", "2", "3", "4", "5")).defaultValue("2").build(),
        GameConditionDto.builder().id("myRank").label("내 랭크").type("select")
            .options(List.of("아이언", "브론즈", "실버", "골드", "플래티넘", "에메랄드", "다이아몬드", "마스터", "그랜드마스터", "챌린저"))
            .defaultValue("실버").dependsOn("mode").dependsOnValues(List.of("랭크", "자유랭크")).build(),
        GameConditionDto.builder().id("rankRange").label("랭크").type("threshold")
            .thresholdOptions(List.of("아이언", "브론즈", "실버", "골드", "플래티넘", "에메랄드", "다이아몬드", "마스터", "그랜드마스터", "챌린저"))
            .defaultValue("아이언~챌린저").dependsOn("mode").dependsOnValues(List.of("랭크", "자유랭크")).build(),
        GameConditionDto.builder().id("position").label("포지션").type("select")
            .options(List.of( "상관없음", "탑", "정글", "미드", "원딜", "서포터")).defaultValue("상관없음")
            .dependsOn("mode").dependsOnValues(List.of("랭크", "자유랭크", "일반게임")).build());

    return GameInfo.builder().id(LoLConstants.GAME_ID).name("League of Legends")
        .description("5v5 팀 기반 전략 게임").playerCount("5명")
        .conditions(lolConditions)
        .build();
  }
}
