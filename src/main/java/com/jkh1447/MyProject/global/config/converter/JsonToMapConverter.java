package com.jkh1447.MyProject.global.config.converter;

import java.util.List;

import com.jkh1447.MyProject.dto.gameInfo.GameConditionDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

@Converter
@RequiredArgsConstructor
public class JsonToMapConverter implements AttributeConverter<List<GameConditionDto>, String> {

  private final ObjectMapper objectMapper;

  // 객체 -> 데이터베이스
  @Override
  public String convertToDatabaseColumn(List<GameConditionDto> attribute) {
    try {
      return objectMapper.writeValueAsString(attribute);
    } catch (Exception e) {
      throw new RuntimeException("JSON 변환 실패", e);
    }
  }

  // 데이터베이스 -> 객체
  @Override
  public List<GameConditionDto> convertToEntityAttribute(String dbData) {
    try {
      return objectMapper.readValue(dbData, new TypeReference<List<GameConditionDto>>() {});
    } catch (Exception e) {
      throw new RuntimeException("JSON 변환 실패", e);
    }
  }
}
