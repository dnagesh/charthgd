package uk.gov.ccew.rsi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "user_entity")
@Data
public class UserEntity {

    private static final int PHONE_NUMBER_MAX_LENGTH = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Enumerated(EnumType.STRING) private Title title;

    private String firstname;

    private String surname;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = PHONE_NUMBER_MAX_LENGTH)
    private String phoneNumber;

}