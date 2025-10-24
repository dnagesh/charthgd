//package com.smartsourcing.charitycommission.rsi;
//
//
//import com.smartsourcing.charitycommission.rsi.repository.FormDataRepository;
//import jakarta.validation.Valid;
//import jakarta.validation.constraints.NotNull;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.validation.annotation.Validated;
//
//import java.util.List;
//
//@Service
//@Validated
//public class ReportIncidentService {
//
//    private final FormDataRepository repository;
//    private final ReportIncidentMapper mapper;
//
//    public ReportIncidentService(FormDataRepository repository, ReportIncidentMapper mapper) {
//        this.repository = repository;
//        this.mapper = mapper;
//    }
//
//    @Transactional
//    public ReportIncident create(@Valid ReportIncident input, String modifiedBy) {
//        if (repository.existsByReferenceCode(input.getReferenceCode())) {
//            throw new IllegalArgumentException("referenceCode already exists: " + input.getReferenceCode());
//        }
//        ReportIncidentEntity entity = mapper.toEntity(input);
//        entity.setModifiedBy(modifiedBy);
//        ReportIncidentEntity saved = repository.save(entity);
//        return mapper.toDomain(saved);
//    }
//
//    @Transactional(readOnly = true)
//    public ReportIncident getById(@NotNull Long id) {
//        ReportIncidentEntity entity = repository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Not found id=" + id));
//        return mapper.toDomain(entity);
//    }
//
//    @Transactional(readOnly = true)
//    public ReportIncident getByReferenceCode(@NotNull String referenceCode) {
//        ReportIncidentEntity entity = repository.findByReferenceCode(referenceCode)
//                .orElseThrow(() -> new IllegalArgumentException("Not found ref=" + referenceCode));
//        return mapper.toDomain(entity);
//    }
//
//    @Transactional(readOnly = true)
//    public List<ReportIncident> listAll() {
//        return repository.findAll().stream().map(mapper::toDomain).toList();
//    }
//
//    @Transactional
//    public ReportIncident update(@Valid ReportIncident input, String modifiedBy) {
//        if (input.getId() == null) {
//            throw new IllegalArgumentException("id is required for update");
//        }
//
//        ReportIncidentEntity entity = repository.findById(input.getId())
//                .orElseThrow(() -> new IllegalArgumentException("Not found id=" + input.getId()));
//
//        // Optional: verify optimistic lock version if client sends it
//        if (input.getVersion() != null && entity.getRVersion() != null &&
//                !entity.getRVersion().equals(input.getVersion())) {
//            throw new IllegalStateException("Version conflict. Please refresh and retry.");
//        }
//
//        mapper.copyToEntity(input, entity);
//        entity.setModifiedBy(modifiedBy);
//        ReportIncidentEntity saved = repository.save(entity);
//        return mapper.toDomain(saved);
//    }
//
//    @Transactional
//    public void delete(@NotNull Long id) {
//        repository.deleteById(id);
//    }
//}
//
