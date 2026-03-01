package com.jkh1447.MyProject.dto.matching;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueueUser {


  private String userId;
  private String queueKey;
  private Double score;

  private Map<String, Object> userInfo = new HashMap<>();

  public String getUserInfoString(String key) {
    return (String) userInfo.getOrDefault(key, "");
  } 


}
