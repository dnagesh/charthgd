package com.smartsourcing.charitycommission.rsi.model;


public class Option {

    public Option() {
        // required by SnakeYAML
    }
    private String label;
    private String nextPage;

    public Option(String label, String nextPage) {
        this.label = label;
        this.nextPage = nextPage;
    }

    public String getLabel() { return label; }
    public String getNext() { return nextPage; }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setNext(String nextPage) {
        this.nextPage = nextPage;
    }
}
