package com.jkh1447.MyProject.service.matching.strategy.PUBG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
import com.jkh1447.MyProject.domain.matching.exception.MatchingErrorCode;
import com.jkh1447.MyProject.domain.matching.exception.UserQueueInfoParsingException;
import com.jkh1447.MyProject.dto.matching.MatchingRequest;
import com.jkh1447.MyProject.dto.matching.QueueInfo;
import com.jkh1447.MyProject.dto.matching.QueueUser;
import com.jkh1447.MyProject.service.matching.strategy.MatchStrategy;
import com.jkh1447.MyProject.service.matching.strategy.PUBG.PUBGConstants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Component
public class PUBGStrategy implements MatchStrategy {

  private final ObjectMapper objectMapper;

  @Override
  public String getGameId() {
    return PUBGConstants.GAME_ID;
  }

  @Override
  public String generateQueueKey(MatchingRequest request) {

    String groupSize = request.filters().getOrDefault(MatchingConstants.MATCH_GROUP_SIZE, "2");
    if (PUBGConstants.GROUP_SIZE_ANY.equals(groupSize))
      groupSize = MatchingConstants.ANY_GROUP_SIZE;
    String gameName = getGameId();
    String mode = request.filters().get(PUBGConstants.GAME_MODE);
    String viewMode = request.filters().get(PUBGConstants.QUEUE_USER_INFO_VIEW_MODE);
    String queueKey =
        MatchingConstants.QUEUE_KEY + ":" + gameName + ":" + "groupSize=" + groupSize + ":" + mode + ":" + viewMode;
    return queueKey;
  }

  @Override
  public String generateAnyQueueKey(String queueKey) {
    return queueKey.replaceAll("groupSize=[^:]+", "groupSize=" + MatchingConstants.ANY_GROUP_SIZE);
  }

  @Override
  public String getQueueUserInfos(MatchingRequest request) {

    Map<String, String> filters = request.filters();
    String mode = filters.get(PUBGConstants.GAME_MODE);

    Map<String, Object> infoMap = new HashMap<>();

    switch (mode) {
      case PUBGConstants.GAME_MODE_RANK:
        infoMap.put(PUBGConstants.QUEUE_USER_INFO_MY_RANK,
            convertRank(filters.get(PUBGConstants.QUEUE_USER_INFO_MY_RANK)));
        infoMap.put(PUBGConstants.QUEUE_USER_INFO_RANK_RANGE,
            convertRankRange(filters.get(PUBGConstants.QUEUE_USER_INFO_RANK_RANGE)));
        break;
      case PUBGConstants.GAME_MODE_NORMAL:
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
    return PUBGConstants.RANK_LEVEL.get(rankName);
  }

  private String convertRankRange(String rawRange) {
    if (rawRange == null || !rawRange.contains("~"))
      return "0~0";
    return Arrays.stream(rawRange.split("~")).map(rank -> PUBGConstants.RANK_LEVEL.get(rank))
        .collect(Collectors.joining("~"));
  }

  @Override
  public List<QueueUser> buildTeam(QueueUser pivot, List<QueueUser> candidates,
      QueueInfo queueInfo) {

    if (PUBGConstants.GAME_MODE_RANK.equals(queueInfo.getMode())) {
      return rankModeTeamBuilding(pivot, candidates, queueInfo);
    }
    else if (PUBGConstants.GAME_MODE_NORMAL.equals(queueInfo.getMode())) {
      return normalModeTeamBuilding(pivot, candidates, queueInfo);
    }
    return null;
  }

  @Override
  public List<QueueUser> buildAnyTeam(QueueUser pivot, List<QueueUser> candidates,
      QueueInfo queueInfo) {

    int groupSize = PUBGConstants.ANY_QUEUE_DEFAULT_GROUP_SIZE;
    List<QueueUser> finalTeam = null;
    queueInfo.setGroupSize(String.valueOf(groupSize)); // 그룹사이즈 5로 설정
    
    if (PUBGConstants.GAME_MODE_RANK.equals(queueInfo.getMode())) {
      finalTeam = rankModeTeamBuilding(pivot, candidates, queueInfo);
    }
    else if (PUBGConstants.GAME_MODE_NORMAL.equals(queueInfo.getMode())) {
      finalTeam = normalModeTeamBuilding(pivot, candidates, queueInfo);
    }

    queueInfo.setGroupSize(String.valueOf(finalTeam.size())); // 최종 그룹사이즈
    return finalTeam;
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
      int candidateRank =
          Integer.parseInt(candidate.getUserInfoString(PUBGConstants.QUEUE_USER_INFO_MY_RANK));
      String candidateRankRange =
          candidate.getUserInfoString(PUBGConstants.QUEUE_USER_INFO_RANK_RANGE);

      // log.info("후보자 랭크 범위: {}", candidateRankRange);
      int candidateRankRangeStart = Integer.parseInt(candidateRankRange.split("~")[0]);
      int candidateRankRangeEnd = Integer.parseInt(candidateRankRange.split("~")[1]);

      boolean isValidRankCondition = true;

      for (QueueUser user : team) {
        
        int userRank =
            Integer.parseInt(user.getUserInfoString(PUBGConstants.QUEUE_USER_INFO_MY_RANK));
        String userRankRange = user.getUserInfoString(PUBGConstants.QUEUE_USER_INFO_RANK_RANGE);
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
