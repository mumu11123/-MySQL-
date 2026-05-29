package com.campus.common.config;

import com.campus.common.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置 — 注册 JWT 拦截器 + 静态资源映射 + SPA 路由
 *
 * @author Campus Team
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    /**
     * SPA 路由支持：非 API/静态资源路径都转发到 index.html
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/admin").setViewName("forward:/index.html");
        registry.addViewController("/admin/**").setViewName("forward:/index.html");
        registry.addViewController("/student").setViewName("forward:/student_login.html");
        registry.addViewController("/login").setViewName("forward:/index.html");
        registry.addViewController("/apply").setViewName("forward:/index.html");
        registry.addViewController("/apply-status").setViewName("forward:/index.html");
        registry.addViewController("/home").setViewName("forward:/index.html");
        registry.addViewController("/merchant/**").setViewName("forward:/index.html");
        registry.addViewController("/dish").setViewName("forward:/index.html");
        registry.addViewController("/order").setViewName("forward:/index.html");
        registry.addViewController("/message").setViewName("forward:/index.html");
        registry.addViewController("/statistics").setViewName("forward:/index.html");
        registry.addViewController("/dish-rank").setViewName("forward:/index.html");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/common/**",
                        "/api/merchant/apply",
                        "/api/merchant/apply/check",
                        "/image/**",
                        "/doc.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                );
    }

    /**
     * 静态资源映射 — 图片访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/image/**")
                .addResourceLocations("file:./image/");
    }
}
