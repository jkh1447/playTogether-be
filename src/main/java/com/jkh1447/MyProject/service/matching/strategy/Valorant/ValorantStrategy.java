package com.jkh1447.MyProject.service.matching.strategy.Valorant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkh1447.MyProject.domain.matching.MatchingConstants;
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
public class ValorantStrategy implements MatchStrategy {

  private final ObjectMapper objectMapper;

  @Override
  public String getGameId() {
    return ValorantConstants.GAME_ID;
  }

  @Override
  public String generateQueueKey(MatchingRequest request) {

    String groupSize = request.filters().getOrDefault(MatchingConstants.MATCH_GROUP_SIZE, "2");
    if (ValorantConstants.GROUP_SIZE_ANY.equals(groupSize))
      groupSize = MatchingConstants.ANY_GROUP_SIZE;
    String gameName = getGameId();
    String mode = request.filters().get(ValorantConstants.GAME_MODE);
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
    String mode = filters.get(ValorantConstants.GAME_MODE);

    Map<String, Object> infoMap = new HashMap<>();

    switch (mode) {
      case ValorantConstants.GAME_MODE_RANK:
        infoMap.put(ValorantConstants.QUEUE_USER_INFO_MY_RANK,
            convertRank(filters.get(ValorantConstants.QUEUE_USER_INFO_MY_RANK)));
        infoMap.put(ValorantConstants.QUEUE_USER_INFO_RANK_RANGE,
            convertRankRange(filters.get(ValorantConstants.QUEUE_USER_INFO_RANK_RANGE)));
        infoMap.put(ValorantConstants.QUEUE_USER_INFO_POSITION,
            filters.get(ValorantConstants.QUEUE_USER_INFO_POSITION));
        break;
      case ValorantConstants.GAME_MODE_NORMAL:
        infoMap.put(ValorantConstants.QUEUE_USER_INFO_POSITION,
            filters.get(ValorantConstants.QUEUE_USER_INFO_POSITION));
        break;
    }

    Map<String, Object> wrapper = Map.of("userInfo", infoMap);

    try {
      return objectMapper.writeValueAsString(wrapper);
    } catch (JsonProcessingException e) {
      log.error("JSON 변환 중 오류 발생", e);
      // 나중에 예외처리
      throw new RuntimeException(e);
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
      int candidateRank =
          Integer.parseInt(candidate.getUserInfoString(LoLConstants.QUEUE_USER_INFO_MY_RANK));
      String candidateRankRange =
          candidate.getUserInfoString(LoLConstants.QUEUE_USER_INFO_RANK_RANGE);
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

  @Override
  public List<QueueUser> buildAnyTeam(QueueUser pivot, List<QueueUser> candidates,
      QueueInfo queueInfo) {
    List<QueueUser> team = new ArrayList<>();
    team.add(pivot);

    int groupSize = LoLConstants.ANY_QUEUE_DEFAULT_GROUP_SIZE;

    for (QueueUser candidate : candidates) {
      if (candidate.getUserId().equals(pivot.getUserId())) {
        // pivot 제외
        continue;
      }
      if (team.size() == groupSize) {
        break;
      }

      String candidatePosition = candidate.getUserInfoString(LoLConstants.QUEUE_USER_INFO_POSITION);
      int candidateRank =
          Integer.parseInt(candidate.getUserInfoString(LoLConstants.QUEUE_USER_INFO_MY_RANK));
      String candidateRankRange =
          candidate.getUserInfoString(LoLConstants.QUEUE_USER_INFO_RANK_RANGE);
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

}
