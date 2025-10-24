package com.smartsourcing.charitycommission.rsi.ui;


public class FormFieldValue {
    private String name;   // question name
    private String value;  // answer

    public FormFieldValue() {}

    public FormFieldValue(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
