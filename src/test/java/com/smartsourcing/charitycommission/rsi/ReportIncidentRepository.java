package com.smartsourcing.charitycommission.rsi;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportIncidentRepository extends JpaRepository<ReportIncidentEntity, Long> {

    Optional<ReportIncidentEntity> findByReferenceCode(String referenceCode);

    boolean existsByReferenceCode(String referenceCode);
}