package com.jkh1447.MyProject.dto.gameInfo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GameConditionDto {
  
  private String id;
  private String label;
  private String type;
  private List<String> options;

  private String placeholder;
  private Integer maxLength;

  private List<String> thresholdOptions;

  private String dependsOn;
  private List<String> dependsOnValues;

  @JsonProperty("default")
  private Object defaultValue;
}
