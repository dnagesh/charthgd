package com.smartsourcing.charitycommission.rsi.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.Map;

@Entity
@Getter
@Setter
public class Submission {

    @Id
    @GeneratedValue
    private long id;

    @Type(JsonType.class)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> inputData;
}
