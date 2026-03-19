package com.jkh1447.MyProject.global.config.gameDatas;

import com.jkh1447.MyProject.domain.gameInfo.GameInfo;
import com.jkh1447.MyProject.dto.gameInfo.GameConditionDto;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(6)
public class UserCreatedGameData implements GameDataComponent {

  @Override
  public String getGameName() {
    return "userCreated";
  }

  @Override
  public GameInfo getGameInfo() {
    List<GameConditionDto> conditions = List.of(
        GameConditionDto.builder()
            .id("userInput")
            .label("게임명")
            .type("input")
            .maxLength(20)
            .placeholder("게임 이름을 공백없이 최대한 정식명칭으로 입력해주세요")
            .defaultValue("")
            .build(),
        GameConditionDto.builder()
            .id("groupSize")
            .label("인원")
            .type("select")
            .options(List.of("상관없음", "2", "3", "4", "5", "6", "7", "8", "9", "10"))
            .defaultValue("2")
            .build()
    );
    return GameInfo.builder()
        .id("userCreated")
        .sortOrder(6)
        .name("직접 입력")
        .playerCount("2~10")
        .description("목록에 없는 게임을 직접 입력하여 참가하세요!")
        .conditions(conditions)
        .build();
  }
}