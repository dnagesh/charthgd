package com.smartsourcing.charitycommission.rsi;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JsonValidationService {

    private final ObjectMapper objectMapper = new ObjectMapper();


    public void validateJson1(String jsonString) {
        try (InputStream schemaStream = getClass().getResourceAsStream("/schema.json")) {
            if (schemaStream == null) {
                throw new IllegalStateException("Schema file '/schema.json' not found in resources.");
            }

            // Read schema safely
            String schemaString;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(schemaStream, StandardCharsets.UTF_8))) {
                schemaString = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }

            // Parse JSON using Gson - Not sure what the value added here.
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

            // Convert to Jackson JsonNode for validation
            JsonNode jsonNode = objectMapper.readTree(jsonObject.toString());

            // Load and validate schema
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
            JsonSchema schema = factory.getSchema(schemaString);
            Set<ValidationMessage> errors = schema.validate(jsonNode);

            if (!errors.isEmpty()) {
                String errorMessages = errors.stream()
                        .map(ValidationMessage::getMessage)
                        .collect(Collectors.joining("\n"));
                throw new IllegalArgumentException("JSON validation failed:\n" + errorMessages);
            }

        } catch (IllegalArgumentException e) {
            throw e; // Re-throw validation errors as-is
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during JSON validation: " + e.getMessage(), e);
        }
    }
}