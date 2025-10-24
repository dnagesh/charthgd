package com.smartsourcing.charitycommission.rsi;

import com.smartsourcing.charitycommission.rsi.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionRepository extends JpaRepository <Submission, Long> {

    Object findAllById(long id);
}
