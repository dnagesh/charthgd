package com.smartsourcing.charitycommission.rsi.service;

import com.smartsourcing.charitycommission.rsi.entity.FormDataEntity;
import com.smartsourcing.charitycommission.rsi.entity.Status;
import com.smartsourcing.charitycommission.rsi.entity.UserEntity;
import com.smartsourcing.charitycommission.rsi.repository.FormDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.annotation.Validated;
import com.smartsourcing.charitycommission.rsi.dto.FormData;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Validated
class RSIServiceTest {

    @Mock
    private FormDataRepository repository;

    @InjectMocks
    private RSIService service;

    private FormData formData;
    private FormDataEntity entity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        formData = new FormData();
        formData.setId(1L);
        formData.setReferenceCode("REF123");
        formData.setStatus("ACTIVE");
        formData.setEmailSent(true);
        formData.setUserAnswers(Map.of("q1", "a1"));

        entity = new FormDataEntity();
        entity.setSubmissionId(1L);
        entity.setReferenceCode("REF123");
        entity.setStatus(Status.CREATED);
        entity.setEmailSent(true);
        entity.setInputData(Map.of("q1", "a1"));
        entity.setUser(new UserEntity());
    }

    @Test
    void shouldCreateNewFormData() {
        when(repository.existsByReferenceCode("REF123")).thenReturn(false);
        when(repository.save(any(FormDataEntity.class))).thenReturn(entity);

        FormData result = service.create(formData);

        assertThat(result).isNotNull();
        assertThat(result.getReferenceCode()).isEqualTo("REF123");
        verify(repository).save(any(FormDataEntity.class));
    }

    @Test
    void shouldThrowExceptionIfReferenceCodeExists() {
        when(repository.existsByReferenceCode("REF123")).thenReturn(true);

        assertThatThrownBy(() -> service.create(formData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("referenceCode already exists");
    }

    @Test
    void shouldGetById() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        FormData result = service.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowExceptionIfIdNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not found id=1");
    }

    @Test
    void shouldGetByReferenceCode() {
        when(repository.findByReferenceCode("REF123")).thenReturn(Optional.of(entity));

        FormData result = service.getByReferenceCode("REF123");

        assertThat(result).isNotNull();
        assertThat(result.getReferenceCode()).isEqualTo("REF123");
    }

    @Test
    void shouldThrowExceptionIfReferenceCodeNotFound() {
        when(repository.findByReferenceCode("REF123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByReferenceCode("REF123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not found ref=REF123");
    }

    @Test
    void shouldListAllFormData() {
        when(repository.findAll()).thenReturn(List.of(entity));

        List<FormData> result = service.listAll();

       // assertThat(result).hasSize(1);
        assertThat(result.getFirst().getReferenceCode()).isEqualTo("REF123");
    }

    @Test
    void shouldUpdateFormData() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any(FormDataEntity.class))).thenReturn(entity);

        FormData result = service.update(formData, "admin");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(repository).save(entity);
    }

    @Test
    void shouldThrowExceptionIfUpdateIdMissing() {
        formData.setId(null);

        assertThatThrownBy(() -> service.update(formData, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id is required for update");
    }

    @Test
    void shouldDeleteById() {
        service.delete(1L);
        verify(repository).deleteById(1L);
    }
}
