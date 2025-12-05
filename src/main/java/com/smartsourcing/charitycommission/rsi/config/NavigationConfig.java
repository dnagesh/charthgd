//package com.smartsourcing.charitycommission.rsi.config;
//
//import com.smartsourcing.charitycommission.rsi.exception.NavigationException;
//import com.smartsourcing.charitycommission.rsi.navigation.model.FormFlow;
//import lombok.Data;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.Resource;
//import org.yaml.snakeyaml.LoaderOptions;
//
//import javax.annotation.PostConstruct;
//import java.io.IOException;
//import java.io.InputStream;
//import java.lang.reflect.Constructor;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.Objects;
//
///**
// * Root configuration class for navigation YAML structure
// */
//@Data
//public class NavigationConfig {
//
//    private FormFlow formFlow;
//
//    private final NavigationProperties navigationProperties;
//
//    @PostConstruct
//    public void loadConfig() {
//
//        try(InputStream inputStream = getInputStream(navigationProperties.getPath())) {
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//    }
//
//    private void loadYaml(InputStream inputStream) {
//        Objects.requireNonNull(inputStream, "Input stream for YAML configuration is null");
//        LoaderOptions loaderOptions = new LoaderOptions();
//        Constructor constructor = new Constructor(FormFlow.class, loaderOptions);
//    }
//
//    private InputStream getInputStream(String path) {
//        Path filePath = Path.of(path);
//
//        try {
//            if (Files.exists(filePath)) {
//                return Files.newInputStream(filePath);
//            }
//
//            Resource resource = new ClassPathResource(path);
//            if (!resource.exists()) {
//                throw new NavigationException("Configuration file not found" + path);
//            }
//            return resource.getInputStream();
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}
//
