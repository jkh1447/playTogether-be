package com.jkh1447.MyProject.global.config.gameDatas;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.jkh1447.MyProject.domain.gameInfo.GameInfo;
import com.jkh1447.MyProject.dto.gameInfo.GameConditionDto;
import com.jkh1447.MyProject.service.matching.strategy.Valorant.ValorantConstants;

import java.util.List;

@Component
@Order(2)
public class ValorantData implements GameDataComponent {

  @Override
  public String getGameName() {
    return ValorantConstants.GAME_ID;
  }

  @Override
  public GameInfo getGameInfo() {
    List<GameConditionDto> valorantConditions = List.of(
        GameConditionDto.builder().id("mode").label("게임 모드").type("select")
            .options(List.of("일반전", "경쟁전")).defaultValue("경쟁전").build(),
        GameConditionDto.builder().id("groupSize").label("인원").type("select")
            .options(List.of("상관없음", "2", "3", "4", "5")).defaultValue("2").build(),
        GameConditionDto.builder().id("myRank").label("내 랭크").type("select")
            .options(List.of("아이언", "브론즈", "실버", "골드", "플래티넘", "다이아몬드", "초월자", "불멸", "레디언트"))
            .defaultValue("골드").dependsOn("mode").dependsOnValues(List.of("경쟁전")).build(),
        GameConditionDto.builder().id("rankRange").label("랭크 범위").type("threshold")
            .thresholdOptions(
                List.of("아이언", "브론즈", "실버", "골드", "플래티넘", "다이아몬드", "초월자", "불멸", "레디언트"))
            .defaultValue("아이언~레디언트").dependsOn("mode").dependsOnValues(List.of("경쟁전")).build());
        // GameConditionDto.builder().id("position").label("포지션").type("select")
        //     .options(List.of("상관없음", "척후대", "감시자", "타격대", "전략가")).defaultValue("상관없음")
        //     .dependsOn("mode").dependsOnValues(List.of("경쟁전", "일반전")).build());


    return GameInfo.builder().id(ValorantConstants.GAME_ID).sortOrder(2).name("Valorant")
        .description("5v5 캐릭터 기반 전술 슈팅 게임").playerCount("5명").conditions(valorantConditions)
        .build();
  }
}
