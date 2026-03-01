package com.jkh1447.MyProject.global.config.gameDatas;

import com.jkh1447.MyProject.service.matching.strategy.AI.AIConstants;
import java.util.List;
import org.springframework.stereotype.Component;
import com.jkh1447.MyProject.domain.gameInfo.GameInfo;
import com.jkh1447.MyProject.dto.gameInfo.GameConditionDto;

@Component
public class AIData implements GameDataComponent {

  @Override
  public String getGameName() {
    return AIConstants.GAME_ID;
  }

  @Override
  public GameInfo getGameInfo() {
    List<GameConditionDto> aiConditions = List.of(
      GameConditionDto.builder().id("userInput").label("게임명").type("input").maxLength(20).placeholder("게임 이름을 최대한 정식명칭으로 입력해주세요").defaultValue("").build(),
      GameConditionDto.builder().id("groupSize").label("인원").type("select")
        .options(List.of("상관없음", "2", "3", "4", "5", "6", "7", "8", "9", "10")).defaultValue("2").build()
    );
    return GameInfo.builder().id(AIConstants.GAME_ID).name("AI 매칭").playerCount("2~10").description("AI를 이용하여 목록에 없는 게임큐에 참가하세요!").conditions(aiConditions).build();
  }
}
