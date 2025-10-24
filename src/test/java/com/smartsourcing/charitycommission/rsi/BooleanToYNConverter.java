package com.smartsourcing.charitycommission.rsi;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Maps Boolean to 'Y'/'N' char(1). */
@Converter
public class BooleanToYNConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        if (attribute == null) return "N";
        return attribute ? "Y" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        return "Y".equalsIgnoreCase(dbData);
    }
}