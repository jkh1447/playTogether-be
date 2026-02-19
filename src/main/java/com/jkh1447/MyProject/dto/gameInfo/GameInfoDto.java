package com.jkh1447.MyProject.dto.gameInfo;

import java.util.List;
import lombok.Builder;
import com.jkh1447.MyProject.domain.gameInfo.GameInfo;

@Builder
public record GameInfoDto(
  String id,
  String name,
  String description,
  String playerCount,
  List<GameConditionDto> conditions
) {
  
  public static GameInfoDto from(GameInfo gameInfo) {
    return GameInfoDto.builder()
        .id(gameInfo.getId())
        .name(gameInfo.getName())
        .description(gameInfo.getDescription())
        .playerCount(gameInfo.getPlayerCount())
        .conditions(gameInfo.getConditions())
        .build();
  }

  public static GameInfoDto fromWithoutConditions(GameInfo gameInfo) {
    return GameInfoDto.builder()
        .id(gameInfo.getId())
        .name(gameInfo.getName())
        .description(gameInfo.getDescription())
        .playerCount(gameInfo.getPlayerCount())
        .conditions(List.of())
        .build();
  }

  public GameInfo toEntity() {
    return GameInfo.builder()
        .id(this.id)
        .name(this.name)
        .description(this.description)
        .playerCount(this.playerCount)
        .conditions(this.conditions)
        .build();
  }
}
