package com.smartsourcing.charitycommission.rsi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI charityCommissionOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:" + serverPort);
        localServer.setDescription("Local Development Server");

        Contact contact = new Contact();
        contact.setName("Smart Sourcing Team");
        contact.setEmail("support@smartsourcing.com");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("UK Charity Commission Search API")
                .version("1.0.0")
                .description("""
                        ## Charity Commission RSI API Documentation
                        
                        This API provides endpoints to search for registered UK charities by:
                        - **Charity Number** (numeric identifier)
                        - **Charity Name** (alphabetic name)
                        
                        ### Features:
                        - 🔍 Search charities by number or name
                        - ✅ Validation of input parameters
                        - 🛡️ Error handling for various scenarios
                        - 📊 Standardized response format
                        
                        ### Test Scenarios (with WireMock):
                        - **Success**: Use charity number `123456` or name `oxfam`
                        - **Not Found**: Use number `999999` or name `nonexistent`
                        - **Server Error**: Use number starting with `500`
                        - **Name Truncation**: Use number `789012`
                        
                        ### Notes:
                        - Maximum charity name length: 50 characters
                        - Input validation: No spaces or special characters (except hyphens in names)
                        """)
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}
