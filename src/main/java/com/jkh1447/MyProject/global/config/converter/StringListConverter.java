package com.jkh1447.MyProject.global.config.converter;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

@Converter
@RequiredArgsConstructor
public class StringListConverter implements AttributeConverter<List<String>, String> {
    private final ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        try {
            // List -> "[1, 2, 3]" (JSON String)
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON writing error", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        try {
            // "[1, 2, 3]" -> List
            return objectMapper.readValue(dbData, List.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON reading error", e);
        }
    }
}