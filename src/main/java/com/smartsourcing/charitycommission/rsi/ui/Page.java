package com.smartsourcing.charitycommission.rsi.model;

import java.util.List;
import java.util.Objects;

public class Page {

    public Page() {
    }
    private String id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page page = (Page) o;
        return Objects.equals(id, page.id) && Objects.equals(sectionTitle, page.sectionTitle) && Objects.equals(text, page.text) && Objects.equals(type, page.type) && Objects.equals(options, page.options) && Objects.equals(next, page.next);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sectionTitle, text, type, options, next);
    }

    private String sectionTitle;
    private String text;
    private String type;
    private List<Option> options;
    private String next;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSectionTitle() { return sectionTitle; }
    public void setSectionTitle(String sectionTitle) { this.sectionTitle = sectionTitle; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<Option> getOptions() { return options; }
    public void setOptions(List<Option> options) { this.options = options; }

    public String getNext() { return next; }
    public void setNext(String next) { this.next = next; }
}

