package uk.gov.ccew.rsi.config;



import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.thymeleaf.spring6.ISpringTemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * Configuration for an {@link ISpringTemplateEngine} to override the source root of Thymeleaf to use the system files.
 * This allows instant update of the page on the browser without a server restart.
 */
@Configuration
@Profile("local")
public class LocalConfig {

    /**
     * Configures the template engine to read Thymeleaf templates directly from the file system.
     * This enables hot reload of template changes without requiring a server restart.
     * <p>
     * The configuration locates the project root by looking up from the classpath resource directory
     * until it finds the build.gradle file, then sets up a {@link FileTemplateResolver} pointing to
     * the src/main/resources/templates/ directory.
     *
     * @param templateEngine the Spring template engine to configure
     * @throws IOException if the application-local.yaml file cannot be read or the project root cannot be found.
     */
    public LocalConfig(final SpringTemplateEngine templateEngine) throws IOException {
        final ClassPathResource applicationYml = new ClassPathResource("application-local.yaml");

        if (applicationYml.isFile()) {
            File sourceRoot = applicationYml.getFile().getParentFile();
            while (Objects.requireNonNull(sourceRoot.listFiles((dir, name) -> name.equals("build.gradle"))).length != 1) {
                sourceRoot = sourceRoot.getParentFile();
            }
            final FileTemplateResolver fileTemplateResolver = new FileTemplateResolver();
            fileTemplateResolver.setPrefix(sourceRoot.getPath() + "/src/main/resources/templates/");
            fileTemplateResolver.setSuffix(".html");
            fileTemplateResolver.setCacheable(false);
            fileTemplateResolver.setCharacterEncoding("UTF-8");
            fileTemplateResolver.setCheckExistence(true);
            fileTemplateResolver.setOrder(1);

            templateEngine.addTemplateResolver(fileTemplateResolver);
        }
    }

}

