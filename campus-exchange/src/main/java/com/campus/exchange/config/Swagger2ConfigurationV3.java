package com.campus.exchange.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 3 配置类
 * 访问路径：/swagger-ui/index.html
 */
@Configuration
public class Swagger2ConfigurationV3 {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("校园二手交换平台 API")
                        .description("基于 SpringDoc OpenAPI 3 接口文档")
                        .version("1.0")
                        .contact(new Contact().name("campus-exchange")))
                .schemaRequirement("Authorization",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization"))
                .addSecurityItem(new SecurityRequirement().addList("Authorization"));
    }
}
