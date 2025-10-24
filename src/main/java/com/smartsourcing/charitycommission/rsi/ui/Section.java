package com.smartsourcing.charitycommission.rsi.model;


import java.util.Map;

public class Section {
    private Map<String, Page> pages;

    public Section() {}

    public Map<String, Page> getPages() {
        return pages;
    }

    public void setPages(Map<String, Page> pages) {
        this.pages = pages;
    }

    @Override
    public String toString() {
        return "Section{" +
                "pages=" + pages +
                '}';
    }
}

