package com.jkh1447.MyProject.service.gameInfo;

import org.springframework.stereotype.Service;
import com.jkh1447.MyProject.repository.gameInfo.gameInfoRepository;
import com.jkh1447.MyProject.dto.gameInfo.GameInfoDto;
import com.jkh1447.MyProject.domain.gameInfo.GameInfo;
import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameInfoService {

  private final gameInfoRepository gameInfoRepository;
  
  public List<GameInfoDto> getAllGameInfoWithoutConditions() {
    List<GameInfo> allGameInfos = gameInfoRepository.findAll();
    return allGameInfos.stream().map(GameInfoDto::fromWithoutConditions).toList();
  }

  public GameInfoDto getGameInfoById(String id) {
    GameInfo gameInfo = gameInfoRepository.findById(id).orElseThrow(() -> new RuntimeException("Game not found"));
    return GameInfoDto.from(gameInfo);
  }
}
