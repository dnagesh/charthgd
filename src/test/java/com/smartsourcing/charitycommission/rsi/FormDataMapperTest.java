package uk.gov.ccew.rsi.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.ccew.rsi.dto.FormData;
import uk.gov.ccew.rsi.entity.FormDataEntity;
import uk.gov.ccew.rsi.entity.Status;
import uk.gov.ccew.rsi.entity.UserEntity;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class FormDataMapperTest {

    @Test
    void shouldMapFormDataToEntity() {
        FormData formData = new FormData();
        formData.setReferenceCode("REF12345");
        formData.setStatus("CREATED");
        formData.setEmail("test@example.com");
        formData.setFirstname("John");
        formData.setSurname("Doe");
        formData.setPhoneNumber("+441234567890");
        formData.setEmailSent(true);

        Map<String, Object> answers = new HashMap<>();
        answers.put("question1", "answer1");
        formData.setUserAnswers(answers);

        FormDataEntity entity = FormDataMapper.mapToEntity(formData);

        assertThat(entity).isNotNull();
        assertThat(entity.getReferenceCode()).isEqualTo("REF12345");
        assertThat(entity.getStatus()).isEqualTo(Status.CREATED);
        assertThat(entity.getEmailSent()).isTrue();
        assertThat(entity.getInputData()).containsEntry("question1", "answer1");

        assertThat(entity.getUser()).isNotNull();
        assertThat(entity.getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(entity.getUser().getFirstname()).isEqualTo("John");
    }

    @Test
    void shouldUpdateExistingEntity() {
        FormData formData = new FormData();
        formData.setReferenceCode("UPDATED123");
        formData.setStatus("UPDATED");
        formData.setEmailSent(false);

        Map<String, Object> answers = new HashMap<>();
        answers.put("updated", "yes");
        formData.setUserAnswers(answers);

        FormDataEntity entity = new FormDataEntity();
        entity.setReferenceCode("OLD123");
        entity.setStatus(Status.UPDATED);
        entity.setEmailSent(true);

        FormDataMapper.updateEntity(formData, entity);

        assertThat(entity.getReferenceCode()).isEqualTo("UPDATED123");
        assertThat(entity.getStatus()).isEqualTo(Status.UPDATED);
        assertThat(entity.getEmailSent()).isFalse();
        assertThat(entity.getInputData()).containsEntry("updated", "yes");
    }

    @Test
    void shouldMapEntityToDto() {
        FormDataEntity entity = getFormDataEntity();

        FormData dto = FormDataMapper.mapToDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getReferenceCode()).isEqualTo("REF987");
        assertThat(dto.getStatus()).isEqualTo("SUBMITTED");
        assertThat(dto.getEmail()).isEqualTo("alice@example.com");
     //   assertThat(dto.getModifiedBy()).isEqualTo("asmith");
        assertThat(dto.getUserAnswers()).containsEntry("q1", "a1");
    }

    private static FormDataEntity getFormDataEntity() {
        FormDataEntity entity = new FormDataEntity();
        entity.setSubmissionId(1L);
        entity.setReferenceCode("REF987");
        entity.setStatus(Status.SUBMITTED);
        entity.setEmailSent(true);

        Map<String, Object> inputData = new HashMap<>();
        inputData.put("q1", "a1");
        entity.setInputData(inputData);

        UserEntity user = new UserEntity();
        user.setFirstname("Alice");
        user.setSurname("Smith");
        user.setEmail("alice@example.com");
        user.setPhoneNumber("07700900982");

        entity.setUser(user);
        return entity;
    }
}