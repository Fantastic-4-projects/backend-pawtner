package com.enigmacamp.pawtner.converter;

import com.enigmacamp.pawtner.dto.request.OperationHoursDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class OperationHoursConverter implements AttributeConverter<OperationHoursDTO, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(OperationHoursDTO attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to convert OperationHoursDTO to JSON", e);
        }
    }

    @Override
    public OperationHoursDTO convertToEntityAttribute(String dbData) {
        if (dbData == null ||  dbData.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, OperationHoursDTO.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert JSON to OperationHoursDTO", e);
        }
    }
}