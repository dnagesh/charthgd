package uk.gov.ccew.rsi.service;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.ccew.rsi.mapper.FormDataMapper;
import uk.gov.ccew.rsi.dto.FormData;
import uk.gov.ccew.rsi.entity.FormDataEntity;
import uk.gov.ccew.rsi.repository.FormDataRepository;

import java.util.List;

@Service
@Validated
public class RSIService {

    private final FormDataRepository repository;

    public RSIService(FormDataRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FormData create(@Valid FormData formData) {
        if (repository.existsByReferenceCode(formData.getReferenceCode())) {
            throw new IllegalArgumentException("referenceCode already exists: " + formData.getReferenceCode());
        }
        FormDataEntity entity = FormDataMapper.mapToEntity(formData);
        FormDataEntity saved = repository.save(entity);
        return FormDataMapper.mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public FormData getById(@NotNull Long id) {
        FormDataEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Not found id=" + id));
        return FormDataMapper.mapToDto(entity);
    }

    @Transactional(readOnly = true)
    public FormData getByReferenceCode(@NotNull String referenceCode) {
        FormDataEntity entity = repository.findByReferenceCode(referenceCode)
                .orElseThrow(() -> new IllegalArgumentException("Not found ref=" + referenceCode));
        return FormDataMapper.mapToDto(entity);
    }

    @Transactional(readOnly = true)
    public List<FormData> listAll() {
        return repository.findAll().stream().map(FormDataMapper::mapToDto).toList();
    }

    @Transactional
    public FormData update(@Valid FormData formData, String modifiedBy) {
        if (formData.getId() == null) {
            throw new IllegalArgumentException("id is required for update");
        }

        FormDataEntity entity = repository.findById(formData.getId())
                .orElseThrow(() -> new IllegalArgumentException("Not found id=" + formData.getId()));

        FormDataMapper.updateEntity(formData, entity);
        FormDataEntity saved = repository.save(entity);
        return FormDataMapper.mapToDto(saved);
    }

    @Transactional
    public void delete(@NotNull Long id) {
        repository.deleteById(id);
    }
}
