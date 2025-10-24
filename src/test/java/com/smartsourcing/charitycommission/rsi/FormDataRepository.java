package uk.gov.ccew.rsi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.ccew.rsi.entity.FormDataEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FormDataRepository extends JpaRepository<FormDataEntity, Long> {

    Optional<FormDataEntity> findByReferenceCode(String referenceCode);

    boolean existsByReferenceCode(String referenceCode);

    @Query("SELECT f FROM FormDataEntity f WHERE f.modifiedOn < :cutoffDate")
    List<FormDataEntity> findAllOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    void deleteAllBySubmissionIdIn(List<Long> ids);

}
