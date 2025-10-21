package uk.gov.ccew.rsi.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ccew.rsi.model.entity.ReportIncidentEntity;

import java.util.Optional;

public interface ReportIncidentRepository extends JpaRepository<ReportIncidentEntity, Long> {

    Optional<ReportIncidentEntity> findByReferenceCode(String referenceCode);

    boolean existsByReferenceCode(String referenceCode);
}