package com.jkh1447.MyProject.global.config.gameDatas;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import com.jkh1447.MyProject.domain.gameInfo.GameInfo;
import com.jkh1447.MyProject.repository.gameInfo.gameInfoRepository;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private final gameInfoRepository gameInfoRepository;
  private final List<GameDataComponent> gameDataComponents;

  @Override
  public void run(String... args) {
    log.info("게임 정보 초기화");

    gameDataComponents.forEach(gameDataComponent -> {
      saveOrUpdate(gameDataComponent.getGameInfo());
    });
  }

  private void saveOrUpdate(GameInfo gameInfo) {
    gameInfoRepository.save(gameInfo);
  }
}
