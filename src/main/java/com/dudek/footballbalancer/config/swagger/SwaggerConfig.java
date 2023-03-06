package com.dudek.footballbalancer.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("Monolith Service API")
                        .description("This API provides access to basic app resources, such as ROOM/PLAYER/SKILL etc.")
                        .version("0.7.0"));
    }
}
