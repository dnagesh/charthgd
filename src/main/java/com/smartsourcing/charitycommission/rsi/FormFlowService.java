package uk.gov.ccew.rsi.service;


import lombok.Getter;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import uk.gov.ccew.rsi.flow.model.Flow;
import uk.gov.ccew.rsi.flow.model.Page;
import uk.gov.ccew.rsi.flow.model.Section;
import uk.gov.ccew.rsi.flow.model.Sections;

import java.io.InputStream;
import java.util.*;

public class FormFlowService {

    public static final String FLOW_YML = "two.yml";

    @Getter
    private final Sections sections;

    private final Navigator navigator = new Navigator();

    public FormFlowService() {
        Yaml yaml = new Yaml(new Constructor(Sections.class, new LoaderOptions()));
        InputStream input = getClass().getClassLoader().getResourceAsStream(FLOW_YML);
        this.sections = yaml.load(input);
    }

    public static void main(String[] args) {

        FormFlowService service = new FormFlowService();
        System.out.println(service.getSections());

    }
}
