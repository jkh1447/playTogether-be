package com.jkh1447.MyProject.service.matching.strategy;

import com.jkh1447.MyProject.dto.matching.QueueUser;
import com.jkh1447.MyProject.dto.matching.MatchingRequest;
import com.jkh1447.MyProject.dto.matching.QueueInfo;
import java.util.List;
import java.util.Map;

public interface MatchStrategy {


  /*
   * Redis ZSET 구조 1. Queue - key: "queue:gameName:groupSize=size:mode" - value: userId - score: 대기열
   * 시간
   * 
   * Redis HASH 구조 1. QueueUserInfo - key: "queueUserInfo" - value: {userId, filters(json)}
   */


  public String getGameId();

  public String generateQueueKey(MatchingRequest request);

  default String generateAnyQueueKey(String queueKey) {
    return "Not Supported";
  };

  public String getQueueUserInfos(MatchingRequest request);

  public List<QueueUser> buildTeam(QueueUser pivot, List<QueueUser> candidates,
      QueueInfo queueInfo);

  public List<QueueUser> buildAnyTeam(QueueUser pivot, List<QueueUser> candidates,
      QueueInfo queueInfo);

}
