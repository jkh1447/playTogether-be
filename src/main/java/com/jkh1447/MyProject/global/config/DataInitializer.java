package com.jkh1447.MyProject.global.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import com.jkh1447.MyProject.domain.gameInfo.GameInfo;
import com.jkh1447.MyProject.dto.gameInfo.GameConditionDto;
import com.jkh1447.MyProject.repository.gameInfo.gameInfoRepository;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private final gameInfoRepository gameInfoRepository;

  @Override
  public void run(String... args) {
    log.info("게임 정보 초기화");

    List<GameConditionDto> lolConditions = List.of(
      GameConditionDto.builder().id("mode").label("게임 모드").type("select").options(List.of("랭크", "증바람" ,"자유랭크", "일반게임", "칼바람 나락")).defaultValue("랭크").build(),
      GameConditionDto.builder().id("groupSize").label("인원").type("select").options(List.of("2", "3", "4", "5")).defaultValue("2").build(),
      GameConditionDto.builder().id("rank").label("랭크").type("threshold").thresholdOptions(List.of("아이언", "브론즈", "실버", "골드", "플래티넘", "에메랄드", "다이아몬드", "마스터+")).defaultValue("아이언~마스터+").dependsOn("mode").dependsOnValues(List.of("랭크", "자유랭크")).build(),
      GameConditionDto.builder().id("position").label("포지션").type("select").options(List.of("탑", "정글", "미드", "원딜", "서포터", "상관없음")).defaultValue("상관없음").dependsOn("mode").dependsOnValues(List.of("랭크", "자유랭크", "일반게임")).build(),
      GameConditionDto.builder().id("voice").label("보이스 사용").type("toggle").defaultValue("false").build()
    );

    GameInfo lol = GameInfo.builder().id("lol").name("League of Legends")
        .description("5v5 팀 기반 전략 게임").playerCount("5명")
        .conditions(lolConditions)
        .build();

    saveOrUpdate(lol);
  }

  private void saveOrUpdate(GameInfo gameInfo) {
    gameInfoRepository.save(gameInfo);
  }
}
