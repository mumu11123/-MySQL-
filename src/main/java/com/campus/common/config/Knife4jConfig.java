package com.campus.common.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j（Swagger / OpenAPI 3）配置
 * 校园自助点餐系统 - 数据库课程设计
 *
 * @author Campus Team
 */
@Configuration
@io.swagger.v3.oas.annotations.security.SecurityScheme(
        name = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "输入登录返回的纯 Token（不要加 Bearer 前缀）"
)
public class Knife4jConfig {

    @Bean
    public OpenAPI campusDiningOpenAPI() {
        String schemeName = "BearerAuth";

        return new OpenAPI()
                .addServersItem(new Server().url("http://localhost:8080").description("本地开发环境"))
                .info(new Info()
                        .title("校园自助点餐系统 API 文档")
                        .description("校园自助点餐系统 — 后端接口文档\n\n"
                                + "**使用说明**：\n"
                                + "1. 先调用「认证模块 → 商家登录」获取 Token\n"
                                + "2. 点击右上角 **Authorize** 按钮，**输入纯 Token**（不要加 Bearer 前缀），点击 Authorize 确认\n"
                                + "3. 看到锁图标变为闭合状态后，再调用其他接口会自动携带 Token")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Campus Team")
                                .email("team@campus.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))

                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components()
                        .addSecuritySchemes(schemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("请先登录获取纯Token，直接粘贴到下方输入框即可")));
    }
}