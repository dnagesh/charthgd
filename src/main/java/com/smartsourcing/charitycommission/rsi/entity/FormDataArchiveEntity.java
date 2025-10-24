package uk.gov.ccew.rsi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class FormDataArchiveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long submissionId;

    @Column(length = 8, nullable = false, unique = true)
    private String referenceCode;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private Boolean emailSent;

}