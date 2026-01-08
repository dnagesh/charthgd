package com.smartsourcing.charitycommission.rsi;

import com.smartsourcing.charitycommission.rsi.config.WireMockConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableConfigurationProperties(WireMockConfig.class)
@EnableCaching
public class RSIApplication {

	public static void main(String[] args) {
		SpringApplication.run(RSIApplication.class, args);
	}

}
