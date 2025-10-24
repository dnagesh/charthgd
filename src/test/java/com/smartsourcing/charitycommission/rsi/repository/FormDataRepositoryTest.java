package com.smartsourcing.charitycommission.rsi.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import uk.gov.ccew.rsi.entity.FormDataEntity;
import uk.gov.ccew.rsi.entity.Status;
import uk.gov.ccew.rsi.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FormDataRepositoryTest {

    @Autowired
    private FormDataRepository formDataRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity createAndSaveUser(String email) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        return userRepository.save(user);
    }

    private FormDataEntity createAndSaveFormData(String referenceCode, Status status, boolean emailSent, LocalDateTime modifiedOn, UserEntity user) {
        FormDataEntity entity = new FormDataEntity();
        entity.setReferenceCode(referenceCode);
        entity.setStatus(status);
        entity.setEmailSent(emailSent);
        entity.setModifiedOn(modifiedOn);
        entity.setUser(user);
        return formDataRepository.save(entity);
    }

    @Test
    void testCreateAndFindByReferenceCode() {
        UserEntity user = createAndSaveUser("test@charitycommission.gov.uk");
        FormDataEntity incident = createAndSaveFormData("AB12CD34", Status.CREATED, false, LocalDateTime.now(), user);

        Optional<FormDataEntity> found = formDataRepository.findByReferenceCode("AB12CD34");
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(Status.CREATED);

        Optional<UserEntity> savedUser = userRepository.findById(user.getUserId());
        assertThat(savedUser).hasValue(user);
    }

    @Test
    void testUpdateFormDataEntity() {
        UserEntity user = createAndSaveUser("test@charitycommission.gov.uk");
        FormDataEntity incident = createAndSaveFormData("XY99ZZ88", Status.CREATED, false, LocalDateTime.now(), user);

        Optional<FormDataEntity> found = formDataRepository.findByReferenceCode("XY99ZZ88");
        assertThat(found).isPresent();

        FormDataEntity toUpdate = found.get();
        toUpdate.setStatus(Status.UPDATED);
        toUpdate.setEmailSent(true);
        formDataRepository.save(toUpdate);

        Optional<FormDataEntity> updated = formDataRepository.findByReferenceCode("XY99ZZ88");
        assertThat(updated).isPresent();
        assertThat(updated.get().getStatus()).isEqualTo(Status.UPDATED);
        assertThat(updated.get().getEmailSent()).isTrue();

        Optional<UserEntity> savedUser = userRepository.findById(user.getUserId());
        assertThat(savedUser).hasValue(user);
    }

    @Test
    void testExistsByReferenceCode() {
        UserEntity user = createAndSaveUser("user@charitycommission.gov.uk");
        createAndSaveFormData("XY98ZA76", Status.SUBMITTED, false, LocalDateTime.now(), user);

        boolean exists = formDataRepository.existsByReferenceCode("XY98ZA76");
        assertThat(exists).isTrue();
    }

    @Test
    void testDeleteFormDataEntity() {
        UserEntity user = createAndSaveUser("delete@example.com");
        FormDataEntity incident = createAndSaveFormData("DEL12345", Status.DELETED, false, LocalDateTime.now(), user);

        formDataRepository.deleteById(incident.getSubmissionId());

        Optional<FormDataEntity> deleted = formDataRepository.findById(incident.getSubmissionId());
        assertThat(deleted).isEmpty();

        Optional<UserEntity> savedUser = userRepository.findById(user.getUserId());
        assertThat(savedUser).hasValue(user);
    }

    @Test
    void testFindAllOlderThan_givenCutoffDate_returns1Record() {
        UserEntity user = createAndSaveUser("test@charitycommission.gov.uk");

        // Recent record
        createAndSaveFormData("RE12CE34", Status.CREATED, false, LocalDateTime.now().minusDays(29), user);

        // Old record
        createAndSaveFormData("OL45DI78", Status.CREATED, false, LocalDateTime.now().minusDays(40), user);

        formDataRepository.flush();

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<FormDataEntity> results = formDataRepository.findAllOlderThan(cutoffDate);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getReferenceCode()).isEqualTo("OL45DI78");
    }
}