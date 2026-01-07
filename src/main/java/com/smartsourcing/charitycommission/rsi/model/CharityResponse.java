package com.smartsourcing.charitycommission.rsi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharityResponse {
    private String charityName;
    private String charityNumber;
    private String registeredCharityNumber;
    private String registrationStatus;
}

