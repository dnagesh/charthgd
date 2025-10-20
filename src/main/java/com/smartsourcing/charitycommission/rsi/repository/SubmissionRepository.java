package com.smartsourcing.charitycommission.rsi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.ccew.rsi.model.entity.Submission;

@Repository
public interface SubmissionRepository extends JpaRepository <Submission, Long> {

    Object findAllById(long id);
}
