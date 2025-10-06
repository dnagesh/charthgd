package com.smartsourcing.charitycommission.rsi.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class JsonMapper {

    public String convertToJson(Map<String, String> data) throws JsonProcessingException {
        String result = null;
        if (data !=null) {
            ObjectMapper objectMapper = new ObjectMapper();
            result = objectMapper.writeValueAsString(data);
        }
        return result;
    }
}
