package com.smartsourcing.charitycommission.rsi.model;

public class Option {
    private String value;
    private String label;

    public Option(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() { return value; }
    public String getLabel() { return label; }
}
