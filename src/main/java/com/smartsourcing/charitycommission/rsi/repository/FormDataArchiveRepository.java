package uk.gov.ccew.rsi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ccew.rsi.entity.FormDataArchiveEntity;

public interface FormDataArchiveRepository extends JpaRepository<FormDataArchiveEntity, Long> {
}
