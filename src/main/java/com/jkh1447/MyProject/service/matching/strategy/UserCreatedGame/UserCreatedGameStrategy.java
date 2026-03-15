package com.jkh1447.MyProject.service.matching.strategy.UserCreatedGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import com.jkh1447.MyProject.domain.matching.exception.MatchingErrorCode;
import com.jkh1447.MyProject.domain.matching.exception.UserQueueInfoParsingException;
import com.jkh1447.MyProject.dto.matching.MatchingRequest;
import com.jkh1447.MyProject.dto.matching.QueueInfo;
import com.jkh1447.MyProject.dto.matching.QueueUser;
import com.jkh1447.MyProject.service.matching.strategy.LoL.LoLConstants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.jkh1447.MyProject.service.matching.strategy.MatchStrategy;
import org.springframework.stereotype.Component;
import com.jkh1447.MyProject.service.matching.aiMatching.AIMatchingService;

@Slf4j
@AllArgsConstructor
@Component
public class UserCreatedGameStrategy implements MatchStrategy {

  private final ObjectMapper objectMapper;
  private final AIMatchingService aiMatchingService;

  @Override
  public String getGameId() {
    return UserCreatedGameConstants.GAME_ID;
  }

  @Override
  public String generateQueueKey(MatchingRequest request) {

    String groupSize = request.filters().getOrDefault(MatchingConstants.MATCH_GROUP_SIZE, "2");
    if (UserCreatedGameConstants.GROUP_SIZE_ANY.equals(groupSize))
      groupSize = MatchingConstants.ANY_GROUP_SIZE;
    String gameName = UserCreatedGameConstants.GAME_ID;
    String mode = request.filters().getOrDefault(MatchingConstants.USER_CREATED_GAME_USER_INPUT, "NO_NAME");
    String queueKey =
        MatchingConstants.QUEUE_KEY + ":" + gameName + ":" + "groupSize=" + groupSize + ":" + mode;
    return queueKey;
  }

  @Override
  public String generateAnyQueueKey(String queueKey) {
    return queueKey.replaceAll("groupSize=[^:]+", "groupSize=" + MatchingConstants.ANY_GROUP_SIZE);
  }

  @Override
  public String getQueueUserInfos(MatchingRequest request) {

    Map<String, Object> infoMap = new HashMap<>();

    Map<String, Object> wrapper = Map.of("userInfo", infoMap);

    try {
      return objectMapper.writeValueAsString(wrapper);
    } catch (JsonProcessingException e) {
      log.error("JSON 변환 중 오류 발생", e);
      // 나중에 예외처리
      throw new UserQueueInfoParsingException(MatchingErrorCode.INVALID_QUEUE_USER_INFO_FORMAT);
    }

  }

  @Override
  public List<QueueUser> buildTeam(QueueUser pivot, List<QueueUser> candidates,
      QueueInfo queueInfo) {

    return noConditionTeamBuilding(pivot, candidates, queueInfo);
  }

  @Override
  public List<QueueUser> buildAnyTeam(QueueUser pivot, List<QueueUser> candidates,
      QueueInfo queueInfo) {

    int groupSize = LoLConstants.ANY_QUEUE_DEFAULT_GROUP_SIZE;
    List<QueueUser> finalTeam = null;
    queueInfo.setGroupSize(String.valueOf(groupSize)); // 그룹사이즈 5로 설정
  
    finalTeam = noConditionTeamBuilding(pivot, candidates, queueInfo);
    
    queueInfo.setGroupSize(String.valueOf(finalTeam.size())); // 최종 그룹사이즈
    return finalTeam;
  }

  private List<QueueUser> noConditionTeamBuilding(QueueUser pivot, List<QueueUser> candidates, QueueInfo queueInfo) {
    List<QueueUser> team = new ArrayList<>();
    team.add(pivot);

    for (QueueUser candidate : candidates) {
      if (candidate.getUserId().equals(pivot.getUserId())) {
        // pivot 제외
        continue;
      }
      if (team.size() == Integer.parseInt(queueInfo.getGroupSize())) {
        break;
      }

      team.add(candidate);
    }

    return team;
  }

 
}
