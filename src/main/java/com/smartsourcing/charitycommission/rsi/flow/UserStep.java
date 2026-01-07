package uk.gov.ccew.rsi.flow.model;


import java.util.Map;


public record UserStep(String taskID,
                       String taskDefinitionKey,
                       String processInstanceID,
                       String formKey,
                       Map<String, Object> formData) {}
