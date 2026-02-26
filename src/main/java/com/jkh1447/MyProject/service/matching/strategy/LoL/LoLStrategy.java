package com.jkh1447.MyProject.service.matching.strategy.LoL;

import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import com.jkh1447.MyProject.domain.matching.exception.MatchingErrorCode;
import com.jkh1447.MyProject.domain.matching.exception.UserQueueInfoParsingException;
import com.jkh1447.MyProject.dto.matching.QueueUser;
import com.jkh1447.MyProject.dto.matching.MatchingRequest;
import com.jkh1447.MyProject.service.matching.strategy.MatchStrategy;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.jkh1447.MyProject.dto.matching.QueueInfo;
import java.util.HashMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class LoLStrategy implements MatchStrategy {

  private final ObjectMapper objectMapper;

  @Override
  public String getGameId() {
    return LoLConstants.GAME_ID;
  }

  @Override
  public String generateQueueKey(MatchingRequest request) {

    String groupSize = request.filters().getOrDefault(MatchingConstants.MATCH_GROUP_SIZE, "2");
    if (LoLConstants.GROUP_SIZE_ANY.equals(groupSize))
      groupSize = MatchingConstants.ANY_GROUP_SIZE;
    String gameName = getGameId();
    String mode = request.filters().get(LoLConstants.GAME_MODE);
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
      case LoLConstants.GAME_MODE_RANK:
        infoMap.put(LoLConstants.QUEUE_USER_INFO_MY_RANK,
            convertRank(filters.get(LoLConstants.QUEUE_USER_INFO_MY_RANK)));
        infoMap.put(LoLConstants.QUEUE_USER_INFO_RANK_RANGE,
            convertRankRange(filters.get(LoLConstants.QUEUE_USER_INFO_RANK_RANGE)));
        infoMap.put(LoLConstants.QUEUE_USER_INFO_POSITION,
            filters.get(LoLConstants.QUEUE_USER_INFO_POSITION));
        break;
      case LoLConstants.GAME_MODE_ARAMCHAOS:
        break;
      case LoLConstants.GAME_MODE_FLEX:
        infoMap.put(LoLConstants.QUEUE_USER_INFO_MY_RANK,
            convertRank(filters.get(LoLConstants.QUEUE_USER_INFO_MY_RANK)));
        infoMap.put(LoLConstants.QUEUE_USER_INFO_RANK_RANGE,
            convertRankRange(filters.get(LoLConstants.QUEUE_USER_INFO_RANK_RANGE)));
        infoMap.put(LoLConstants.QUEUE_USER_INFO_POSITION,
            filters.get(LoLConstants.QUEUE_USER_INFO_POSITION));
        break;
      case LoLConstants.GAME_MODE_NORMAL:
        infoMap.put(LoLConstants.QUEUE_USER_INFO_POSITION,
            filters.get(LoLConstants.QUEUE_USER_INFO_POSITION));
        break;
      case LoLConstants.GAME_MODE_ARAM:
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
    return LoLConstants.RANK_LEVEL.get(rankName);
  }

  private String convertRankRange(String rawRange) {
    if (rawRange == null || !rawRange.contains("~"))
      return "0~0";
    return Arrays.stream(rawRange.split("~")).map(rank -> LoLConstants.RANK_LEVEL.get(rank))
        .collect(Collectors.joining("~"));
  }

  @Override
  public List<QueueUser> buildTeam(QueueUser pivot, List<QueueUser> candidates,
      QueueInfo queueInfo) {

    if (LoLConstants.GAME_MODE_RANK.equals(queueInfo.getMode()) || LoLConstants.GAME_MODE_FLEX.equals(queueInfo.getMode())) {
      return rankModeTeamBuilding(pivot, candidates, queueInfo);
    }
    else if (LoLConstants.GAME_MODE_NORMAL.equals(queueInfo.getMode())) {
      return normalModeTeamBuilding(pivot, candidates, queueInfo);
    }
    else {
      return noConditionTeamBuilding(pivot, candidates, queueInfo);
    }
  }

  @Override
  public List<QueueUser> buildAnyTeam(QueueUser pivot, List<QueueUser> candidates,
      QueueInfo queueInfo) {

    int groupSize = LoLConstants.ANY_QUEUE_DEFAULT_GROUP_SIZE;
    List<QueueUser> finalTeam = null;
    queueInfo.setGroupSize(String.valueOf(groupSize)); // 그룹사이즈 5로 설정
    
    if (LoLConstants.GAME_MODE_RANK.equals(queueInfo.getMode()) || LoLConstants.GAME_MODE_FLEX.equals(queueInfo.getMode())) {
      finalTeam = rankModeTeamBuilding(pivot, candidates, queueInfo);
    }
    else if (LoLConstants.GAME_MODE_NORMAL.equals(queueInfo.getMode())) {
      finalTeam = normalModeTeamBuilding(pivot, candidates, queueInfo);
    }
    else {
      finalTeam = noConditionTeamBuilding(pivot, candidates, queueInfo);
    }

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

  private List<QueueUser> rankModeTeamBuilding(QueueUser pivot, List<QueueUser> candidates, QueueInfo queueInfo) {

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
      String candidatePosition = candidate.getUserInfoString(LoLConstants.QUEUE_USER_INFO_POSITION);
      int candidateRank =
          Integer.parseInt(candidate.getUserInfoString(LoLConstants.QUEUE_USER_INFO_MY_RANK));
      String candidateRankRange =
          candidate.getUserInfoString(LoLConstants.QUEUE_USER_INFO_RANK_RANGE);

      // log.info("후보자 랭크 범위: {}", candidateRankRange);
      int candidateRankRangeStart = Integer.parseInt(candidateRankRange.split("~")[0]);
      int candidateRankRangeEnd = Integer.parseInt(candidateRankRange.split("~")[1]);

      boolean isSamePosition = false;
      boolean isValidRankCondition = true;

      for (QueueUser user : team) {
        String userPosition = user.getUserInfoString(LoLConstants.QUEUE_USER_INFO_POSITION);
        if (!LoLConstants.POSITION_ANY.equals(userPosition)
            && !LoLConstants.POSITION_ANY.equals(candidatePosition)
            && candidatePosition.equals(userPosition)) { // 포지션이 같으면
          isSamePosition = true;
          break;
        }

        int userRank =
            Integer.parseInt(user.getUserInfoString(LoLConstants.QUEUE_USER_INFO_MY_RANK));
        String userRankRange = user.getUserInfoString(LoLConstants.QUEUE_USER_INFO_RANK_RANGE);
        int userRankRangeStart = Integer.parseInt(userRankRange.split("~")[0]);
        int userRankRangeEnd = Integer.parseInt(userRankRange.split("~")[1]);

        // 후보자의 랭크 범위에 팀원의 랭크가 포함되는지 확인
        if ((candidateRankRangeStart > userRank || userRank > candidateRankRangeEnd)
            || (userRankRangeStart > candidateRank || candidateRank > userRankRangeEnd)) {
          isValidRankCondition = false;
          break;
        }

      }

      if (isSamePosition || !isValidRankCondition) {
        continue;
      }

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

      String candidatePosition = candidate.getUserInfoString(LoLConstants.QUEUE_USER_INFO_POSITION);

      boolean isSamePosition = false;

      for (QueueUser user : team) {
        String userPosition = user.getUserInfoString(LoLConstants.QUEUE_USER_INFO_POSITION);
        if (!LoLConstants.POSITION_ANY.equals(userPosition)
            && !LoLConstants.POSITION_ANY.equals(candidatePosition)
            && candidatePosition.equals(userPosition)) { // 포지션이 같으면
          isSamePosition = true;
          break;
        }

      }

      if (isSamePosition) {
        continue;
      }

      team.add(candidate);
    }

    return team;
  }
}
