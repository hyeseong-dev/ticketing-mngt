package com.mgnt.common.config;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("티켓팅 API - 항해 리부트 백엔드 3기 예약 구매 프로젝트")
                        .description("콘서트 좌석을 예약한다.")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes("bearer-key",
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    @Bean
    public OperationCustomizer globalHeader() {
        return (operation, handlerMethod) -> {
            operation.addParametersItem(new Parameter()
                    .in(ParameterIn.HEADER.toString())
                    .schema(new StringSchema().name("ReAuthorization"))
                    .name("ReAuthorization"));
            return operation;
        };
    }
}
