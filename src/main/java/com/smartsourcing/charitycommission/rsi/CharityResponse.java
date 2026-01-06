package uk.gov.ccew.rsi.charity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class CharityResponse {
    @Getter
    @Setter
    private String charityName;
    @Getter
    private final String charityNameNumber;
    @Getter
    private final String regCharityNumber;
    @Getter
    private final String regStatus;


}

