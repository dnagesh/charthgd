package com.smartsourcing.charitycommission.rsi.navigation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormFlow {

    private List<Section> sections;
}
