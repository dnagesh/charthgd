package com.smartsourcing.charitycommission.rsi.controller;

import com.smartsourcing.charitycommission.rsi.model.CharityResponse;
import com.smartsourcing.charitycommission.rsi.service.CharityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/charity")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Charity Search API", description = "REST API endpoints for searching UK registered charities")
public class CharityRestController {

    private final CharityService charityService;

    @Operation(summary = "Search charity by number")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Charity found successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CharityResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "charityName": "Example Charity Foundation",
                                                "charityNumber": "123456",
                                                "registeredCharityNumber": "REG-123456",
                                                "registrationStatus": "ACTIVE"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Charity not found"),
            @ApiResponse(responseCode = "503", description = "Service unavailable")
    })
    @GetMapping("/number/{charityNumber}")
    public ResponseEntity<CharityResponse> searchByNumber(
            @Parameter(description = "Charity registration number", example = "123456", required = true)
            @PathVariable String charityNumber
    ) {
        log.info("REST API: Searching charity by number: {}", charityNumber);

        if (!charityNumber.matches("\\d+")) {
            throw new IllegalArgumentException("Charity number must contain only digits");
        }

        CharityResponse response = charityService.getByNumber(charityNumber);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search charity by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Charity found successfully"),
            @ApiResponse(responseCode = "404", description = "Charity not found"),
            @ApiResponse(responseCode = "503", description = "Service unavailable")
    })
    @GetMapping("/name/{charityName}")
    public ResponseEntity<CharityResponse> searchByName(
            @Parameter(description = "Charity name", example = "oxfam", required = true)
            @PathVariable String charityName
    ) {
        log.info("REST API: Searching charity by name: {}", charityName);

        if (!charityName.matches("[A-Za-z-]+")) {
            throw new IllegalArgumentException("Charity name must contain only letters and hyphens");
        }

        CharityResponse response = charityService.getByName(charityName);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Universal charity search")
    @GetMapping("/search")
    public ResponseEntity<CharityResponse> search(
            @Parameter(description = "Charity number or name", example = "123456", required = true)
            @RequestParam String query
    ) {
        log.info("REST API: Universal search with query: {}", query);

        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }

        CharityResponse response;

        if (query.matches("\\d+")) {
            response = charityService.getByNumber(query);
        } else if (query.matches("[A-Za-z-]+")) {
            response = charityService.getByName(query);
        } else {
            throw new IllegalArgumentException(
                    "Invalid input format. Must be either all digits or letters/hyphens"
            );
        }

        return ResponseEntity.ok(response);
    }
}
