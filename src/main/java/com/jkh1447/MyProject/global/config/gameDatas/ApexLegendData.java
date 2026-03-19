package com.jkh1447.MyProject.global.config.gameDatas;

import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.jkh1447.MyProject.domain.gameInfo.GameInfo;
import com.jkh1447.MyProject.dto.gameInfo.GameConditionDto;
import com.jkh1447.MyProject.service.matching.strategy.ApexLegend.ApexLegendConstants;

@Component
@Order(5)
public class ApexLegendData implements GameDataComponent {
  @Override
  public String getGameName() {
    return ApexLegendConstants.GAME_ID;
  }

  @Override
  public GameInfo getGameInfo() {

    List<GameConditionDto> apexLegendConditions = List.of(
      GameConditionDto.builder().id("mode").label("게임 모드").type("select")
        .options(List.of("일반전", "랭크")).defaultValue("랭크").build(),
      GameConditionDto.builder().id("groupSize").label("인원").type("select")
        .options(List.of("상관없음", "2", "3")).defaultValue("2").build(),
      GameConditionDto.builder().id("myRank").label("내 랭크").type("select")
        .options(List.of("브론즈", "실버", "골드", "플래티넘", "다이아몬드", "프레데터"))
        .defaultValue("골드").dependsOn("mode").dependsOnValues(List.of("랭크")).build(),
      GameConditionDto.builder().id("rankRange").label("랭크 범위").type("threshold")
        .thresholdOptions(List.of("브론즈", "실버", "골드", "플래티넘", "다이아몬드", "프레데터"))
        .defaultValue("브론즈~프레데터").dependsOn("mode").dependsOnValues(List.of("랭크")).build()
    );

    return GameInfo.builder().id(ApexLegendConstants.GAME_ID).sortOrder(5).name("ApexLegend")
        .description("1인칭 배틀로얄 슈팅 게임").playerCount("3명").conditions(apexLegendConditions).build();
  }
}
