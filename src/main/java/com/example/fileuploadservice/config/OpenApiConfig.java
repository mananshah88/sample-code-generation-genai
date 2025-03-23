package com.example.fileuploadservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI fileUploadOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("File Upload Service API")
                        .description("REST API for uploading and managing files against order IDs")
                        .version("1.0"));
    }
}
