package uk.gov.ccew.rsi.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.ccew.rsi.service.JsonValidationService;

@RestController
@RequestMapping("/validate")
public class JsonValidationController {

    private final JsonValidationService validationService;

    public JsonValidationController(JsonValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping
    public ResponseEntity<String> validateJson(@RequestBody String jsonPayload) {
        try {
            validationService.validateJson(jsonPayload);
            return ResponseEntity.ok("JSON is valid.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        }
    }
}

