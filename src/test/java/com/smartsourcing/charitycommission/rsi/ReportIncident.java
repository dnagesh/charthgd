package com.smartsourcing.charitycommission.rsi;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class ReportIncident {

    // Keep id nullable for create; present for updates
    private Long id;

    @NotBlank
    @Size(max = 8)
    private String referenceCode;

    @NotBlank
    @Email
    @Size(max = 25)
    private String email;

    // should we capture this as a separate column
    @Pattern(regexp = "^[+]?\\d{7,20}$", message = "Phone must be 7-20 digits, optional leading +")
    private String phoneNumber;

    @NotBlank
    @Size(max = 10)
    private String status;

    // If required, add @NotNull
    private Map<String, Object> questionAnswers;

    @NotNull
    private Boolean emailSent;

    // Read-only fields exposed if needed (e.g., in GET)
    private Instant modifiedOn;
    private Instant version; // maps r_version

    @Size(max = 50)
    private String modifiedBy;

}

