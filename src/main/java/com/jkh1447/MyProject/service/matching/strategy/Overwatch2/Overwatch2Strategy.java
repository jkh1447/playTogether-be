package com.jkh1447.MyProject.service.matching.strategy.Overwatch2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import com.jkh1447.MyProject.domain.matching.exception.MatchingErrorCode;
import com.jkh1447.MyProject.domain.matching.exception.UserQueueInfoParsingException;
import com.jkh1447.MyProject.dto.matching.MatchingRequest;
import com.jkh1447.MyProject.dto.matching.QueueInfo;
import com.jkh1447.MyProject.dto.matching.QueueUser;
import com.jkh1447.MyProject.service.matching.strategy.MatchStrategy;
import com.jkh1447.MyProject.service.matching.strategy.LoL.LoLConstants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class Overwatch2Strategy implements MatchStrategy {
  
  private final ObjectMapper objectMapper;

  @Override
  public String getGameId() {
    return Overwatch2Constants.GAME_ID;
  }

  @Override
  public String generateQueueKey(MatchingRequest request) {

    String groupSize = request.filters().getOrDefault(MatchingConstants.MATCH_GROUP_SIZE, "2");
    if (Overwatch2Constants.GROUP_SIZE_ANY.equals(groupSize))
      groupSize = MatchingConstants.ANY_GROUP_SIZE;
    String gameName = getGameId();
    String mode = request.filters().get(Overwatch2Constants.GAME_MODE);
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

    Map<String, String> filters = request.filters();
    String mode = filters.get(LoLConstants.GAME_MODE);

    Map<String, Object> infoMap = new HashMap<>();

    switch (mode) {
      case Overwatch2Constants.GAME_MODE_RANK:
        infoMap.put(Overwatch2Constants.QUEUE_USER_INFO_MY_RANK,
            convertRank(filters.get(Overwatch2Constants.QUEUE_USER_INFO_MY_RANK)));
        infoMap.put(Overwatch2Constants.QUEUE_USER_INFO_RANK_RANGE,
            convertRankRange(filters.get(Overwatch2Constants.QUEUE_USER_INFO_RANK_RANGE)));
        infoMap.put(Overwatch2Constants.QUEUE_USER_INFO_POSITION,
            filters.get(Overwatch2Constants.QUEUE_USER_INFO_POSITION));
        break;
      case Overwatch2Constants.GAME_MODE_NORMAL:
        break;
      
    }

    Map<String, Object> wrapper = Map.of("userInfo", infoMap);

    try {
      return objectMapper.writeValueAsString(wrapper);
    } catch (JsonProcessingException e) {
      log.error("JSON 변환 중 오류 발생", e);
      // 나중에 예외처리
      throw new UserQueueInfoParsingException(MatchingErrorCode.INVALID_QUEUE_USER_INFO_FORMAT);
    }
  }

  private String convertRank(String rankName) {
    return Overwatch2Constants.RANK_LEVEL.get(rankName);
  }

  private String convertRankRange(String rawRange) {
    if (rawRange == null || !rawRange.contains("~"))
      return "0~0";
    return Arrays.stream(rawRange.split("~")).map(rank -> Overwatch2Constants.RANK_LEVEL.get(rank))
        .collect(Collectors.joining("~"));
  }

  @Override
  public List<QueueUser> buildTeam(QueueUser pivot, List<QueueUser> candidates,
      QueueInfo queueInfo) {

    if (Overwatch2Constants.GAME_MODE_RANK.equals(queueInfo.getMode())) {
      return rankModeTeamBuilding(pivot, candidates, queueInfo);
    }
    else if (Overwatch2Constants.GAME_MODE_NORMAL.equals(queueInfo.getMode())) {
      return normalModeTeamBuilding(pivot, candidates, queueInfo);
    }
    return null;
  }

  @Override
  public List<QueueUser> buildAnyTeam(QueueUser pivot, List<QueueUser> candidates,
      QueueInfo queueInfo) {

    int groupSize = LoLConstants.ANY_QUEUE_DEFAULT_GROUP_SIZE;
    List<QueueUser> finalTeam = null;
    queueInfo.setGroupSize(String.valueOf(groupSize)); // 그룹사이즈 5로 설정
    
    if (Overwatch2Constants.GAME_MODE_RANK.equals(queueInfo.getMode())) {
      finalTeam = rankModeTeamBuilding(pivot, candidates, queueInfo);
    }
    else if (Overwatch2Constants.GAME_MODE_NORMAL.equals(queueInfo.getMode())) {
      finalTeam = normalModeTeamBuilding(pivot, candidates, queueInfo);
    }

    queueInfo.setGroupSize(String.valueOf(finalTeam.size())); // 최종 그룹사이즈
    return finalTeam;
  }

  private List<QueueUser> rankModeTeamBuilding(QueueUser pivot, List<QueueUser> candidates, QueueInfo queueInfo) {

    Map<String, Integer> positionLimit = Map.of(
        Overwatch2Constants.POSITION_TANK, 1,
        Overwatch2Constants.POSITION_DAMAGE, 2,
        Overwatch2Constants.POSITION_SUPPORT, 2,
        Overwatch2Constants.POSITION_ANY, 9999
    );
    Map<String, Integer> currentCounts = new HashMap<>();
    currentCounts.put(pivot.getUserInfoString(Overwatch2Constants.QUEUE_USER_INFO_POSITION), 1);

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
      log.info("후보자 정보: {}", candidate.getUserInfo());
      String candidatePosition = candidate.getUserInfoString(Overwatch2Constants.QUEUE_USER_INFO_POSITION);
      int candidateRank =
          Integer.parseInt(candidate.getUserInfoString(Overwatch2Constants.QUEUE_USER_INFO_MY_RANK));
      String candidateRankRange =
          candidate.getUserInfoString(Overwatch2Constants.QUEUE_USER_INFO_RANK_RANGE);

      // log.info("후보자 랭크 범위: {}", candidateRankRange);
      int candidateRankRangeStart = Integer.parseInt(candidateRankRange.split("~")[0]);
      int candidateRankRangeEnd = Integer.parseInt(candidateRankRange.split("~")[1]);

      boolean isValidRankCondition = true;

      if (positionLimit.get(candidatePosition) < (currentCounts.getOrDefault(candidatePosition, 0) + 1)) {
        continue;
      }


      for (QueueUser user : team) {
        
        int userRank =
            Integer.parseInt(user.getUserInfoString(Overwatch2Constants.QUEUE_USER_INFO_MY_RANK));
        String userRankRange = user.getUserInfoString(Overwatch2Constants.QUEUE_USER_INFO_RANK_RANGE);
        int userRankRangeStart = Integer.parseInt(userRankRange.split("~")[0]);
        int userRankRangeEnd = Integer.parseInt(userRankRange.split("~")[1]);

        // 후보자의 랭크 범위에 팀원의 랭크가 포함되는지 확인
        if ((candidateRankRangeStart > userRank || userRank > candidateRankRangeEnd)
            || (userRankRangeStart > candidateRank || candidateRank > userRankRangeEnd)) {
          isValidRankCondition = false;
          break;
        }

      }

      if (!isValidRankCondition) {
        continue;
      }
      
      currentCounts.put(candidatePosition, currentCounts.getOrDefault(candidatePosition, 0) + 1);
      team.add(candidate);
    }

    return team;
  }

  private List<QueueUser> normalModeTeamBuilding(QueueUser pivot, List<QueueUser> candidates, QueueInfo queueInfo) {
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
