package com.smartsourcing.charitycommission.rsi.mapper;


import com.smartsourcing.charitycommission.rsi.dto.FormData;
import com.smartsourcing.charitycommission.rsi.entity.FormDataEntity;
import com.smartsourcing.charitycommission.rsi.entity.Status;
import com.smartsourcing.charitycommission.rsi.entity.UserEntity;

import java.time.LocalDateTime;

public class FormDataMapper {

    public static FormDataEntity mapToEntity(FormData formData) {
        if (formData == null) {
            return null;
        }
        FormDataEntity entity = new FormDataEntity();
        updateEntity(formData, entity);

        return entity;
    }

    public static void updateEntity(FormData formData, FormDataEntity entity) {

        if (formData == null || entity == null) {
            return;
        }

        entity.setReferenceCode(formData.getReferenceCode());
        if (formData.getStatus() != null) {
            try {
                entity.setStatus(Status.valueOf(formData.getStatus()));
            } catch (IllegalArgumentException e) {
                entity.setStatus(null);
            }
        }

        entity.setInputData(formData.getUserAnswers());
        entity.setUser(mapToUserEntity(formData));
        entity.setEmailSent(formData.getEmailSent());
        entity.setModifiedOn(LocalDateTime.now());
    }


    public static FormData mapToDto(FormDataEntity entity) {
        if (entity == null) {
            return null;
        }

        FormData dto = new FormData();
        dto.setId(entity.getSubmissionId());
        dto.setReferenceCode(entity.getReferenceCode());

        if (entity.getStatus() != null) {
            dto.setStatus(entity.getStatus().name());
        }

        dto.setUserAnswers(entity.getInputData());
        dto.setEmailSent(entity.getEmailSent());

        UserEntity user = entity.getUser();
        if (user != null) {
            dto.setFirstname(user.getFirstname());
            dto.setSurname(user.getSurname());
            dto.setEmail(user.getEmail());
            dto.setPhoneNumber(user.getPhoneNumber());
        }

        return dto;
    }

    private static UserEntity mapToUserEntity(FormData formData) {
        if (formData == null) {
            return null;
        }

        UserEntity user = new UserEntity();
        user.setEmail(formData.getEmail());
        user.setFirstname(formData.getFirstname());
        user.setSurname(formData.getSurname());
        user.setPhoneNumber(formData.getPhoneNumber());

        return user;
    }

}