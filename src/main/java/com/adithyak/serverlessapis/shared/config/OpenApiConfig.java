package com.adithyak.serverlessapis.shared.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Personal Backend API",
                version = "v1",
                description = "Personal REST APIs for todo management, reminders, and expense tracking"
        )
)
@SecurityScheme(
        name = "X-API-Key",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-API-Key",
        description = "API key for authentication. Pass as X-API-Key request header."
)
public class OpenApiConfig {
}
