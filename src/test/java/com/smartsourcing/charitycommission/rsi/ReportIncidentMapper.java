package com.smartsourcing.charitycommission.rsi;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ReportIncidentMapper {

    private final ModelMapper mapper;

    public ReportIncidentMapper(ModelMapper mapper) {
        this.mapper = mapper;

        // Explicit field mapping where names differ
        mapper.typeMap(ReportIncidentEntity.class, ReportIncident.class)
                .addMapping(ReportIncidentEntity::getReportIncidentId, ReportIncident::setId)
                .addMapping(ReportIncidentEntity::getRVersion, ReportIncident::setVersion);

        mapper.typeMap(ReportIncident.class, ReportIncidentEntity.class)
                .addMapping(ReportIncident::getId, ReportIncidentEntity::setReportIncidentId)
                .addMapping(ReportIncident::getVersion, ReportIncidentEntity::setRVersion);
    }

    public ReportIncident toDomain(ReportIncidentEntity entity) {
        return mapper.map(entity, ReportIncident.class);
    }

    public ReportIncidentEntity toEntity(ReportIncident domain) {
        return mapper.map(domain, ReportIncidentEntity.class);
    }

    public void copyToEntity(ReportIncident src, ReportIncidentEntity dest) {
        mapper.map(src, dest);
    }
}

