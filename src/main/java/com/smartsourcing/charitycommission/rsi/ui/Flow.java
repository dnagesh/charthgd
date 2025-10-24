package com.smartsourcing.charitycommission.rsi.ui;

import java.util.Map;

public class Flow {
    private Map<String, Section> sections;

    public Flow() {}

    public Map<String, Section> getSections() {
        return sections;
    }

    public void setSections(Map<String, Section> sections) {
        this.sections = sections;
    }

    @Override
    public String toString() {
        return "Flow{" +
                "sections=" + sections +
                '}';
    }
}

