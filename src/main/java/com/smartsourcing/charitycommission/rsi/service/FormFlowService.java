package com.smartsourcing.charitycommission.rsi.service;

import com.smartsourcing.charitycommission.rsi.ui.Flow;
import com.smartsourcing.charitycommission.rsi.ui.Page;
import com.smartsourcing.charitycommission.rsi.ui.Section;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.Map;

@Service
public class FormFlowService {

    private final Flow flow;

    public FormFlowService() {
        Yaml yaml = new Yaml(new Constructor(Flow.class, new LoaderOptions()));
        InputStream input = getClass().getClassLoader().getResourceAsStream("flow.yml");
        this.flow = yaml.load(input);
    }

    public Page getPage(String sectionName, String pageId) {
        Section section = flow.getSections().get(sectionName);
        if (section == null) {
            throw new IllegalArgumentException("Section not found: " + sectionName);
        }

        Page page = section.getPages().get(pageId);
        if (page == null) {
            throw new IllegalArgumentException("Page not found: " + sectionName + "/" + pageId);
        }

        return page;
    }

    public Flow getFlow() {
        return flow;
    }

    public Map<String, Section> getSections() {
        return flow.getSections();
    }
}
