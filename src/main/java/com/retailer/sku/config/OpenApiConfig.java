package com.retailer.sku.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SKU Management API")
                        .version("1.0.0")
                        .description("API for managing Stock Keeping Units (SKUs) for retail operations")
                        .contact(new Contact()
                                .name("Retail Platform Team")
                                .email("platform@retailer.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://retailer.com/terms")))
                .externalDocs(new ExternalDocumentation()
                        .description("SKU Service Documentation")
                        .url("https://docs.retailer.com/sku-service"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local"),
                        new Server().url("https://api.retailer.com").description("Production")));
    }
}
