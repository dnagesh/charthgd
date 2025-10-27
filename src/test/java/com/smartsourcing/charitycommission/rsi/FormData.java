package uk.gov.ccew.rsi.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class FormData {

    // Keep id nullable for create; present for updates
    private Long id;

    @NotBlank
    @Size(max = 8)
    private String referenceCode;

    private String firstname;

    private String surname;

    @NotBlank
    @Email
    @Size(max = 25)
    private String email;

    @Pattern(regexp = "^\\+?[0-9 ]{7,15}$", message = "Enter a telephone number, like 01632 960 001, 07700 900 982 or +44 0808 157 0192")
    private String phoneNumber;

    @NotBlank
    @Size(max = 10)
    private String status;

    @NotNull
    private Map<String, Object> userAnswers;

    @NotNull
    private Boolean emailSent;

    @Size(max = 50)
    private String modifiedBy;

}
