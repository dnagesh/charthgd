package com.smartsourcing.charitycommission.rsi.model;

import jakarta.validation.constraints.NotBlank;

public class User {

    @NotBlank
    private String name;

    // getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
