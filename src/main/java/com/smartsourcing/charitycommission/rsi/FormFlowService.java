package com.smartsourcing.charitycommission.rsi;


import com.smartsourcing.charitycommission.rsi.navigation.model.Sections;
import lombok.Getter;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;

public class FormFlowService {

    public static final String FLOW_YML = "two.yml";

    @Getter
    private final Sections sections;

//    private final Navigator navigator = new Navigator();

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
