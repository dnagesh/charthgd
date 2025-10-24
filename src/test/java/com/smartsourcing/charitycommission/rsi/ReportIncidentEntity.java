package com.smartsourcing.charitycommission.rsi;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "report_incident")
@Data
public class ReportIncidentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // adjust if your DB uses a different strategy
    @Column(name = "report_incident_id", nullable = false)
    private Long reportIncidentId;

    @Column(name = "reference_code", length = 8, nullable = false, unique = true)
    private String referenceCode;

    @Column(name = "email", length = 25, nullable = false)
    private String email;

    @Column(name = "phone_number", length = 32, nullable = true) // Store as text to preserve leading zeros
    private String phoneNumber;

    @Column(name = "status", length = 10, nullable = false)
    private String status;

    @Convert(converter = MapJsonConverter.class)
    @Column(name = "question_answers", columnDefinition = "TEXT") // Adjust for your DB (e.g., JSON/JSONB)
    private Map<String, Object> questionAnswers;

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "email_sent", columnDefinition = "char(1)", length = 1, nullable = false)
    private Boolean emailSent;

    @Column(name = "modified_on", nullable = false)
    private Instant modifiedOn;

    @Version
    @Column(name = "r_version", nullable = false)
    private Instant rVersion;

    @Column(name = "modified_by", length = 50, nullable = true)
    private String modifiedBy;

    @PrePersist
    @PreUpdate
    public void touch() {
        this.modifiedOn = Instant.now();
    }

}
