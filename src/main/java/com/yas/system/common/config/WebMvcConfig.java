package com.yas.system.common.config;

import com.yas.system.common.security.annotation.ActiveUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final long MAX_AGE_SECS = 3600;

    private final ActiveUserArgumentResolver activeUserArgumentResolver;
    private final AppProperties appProperties;

    public WebMvcConfig(ActiveUserArgumentResolver activeUserArgumentResolver, AppProperties appProperties) {
        this.activeUserArgumentResolver = activeUserArgumentResolver;
        this.appProperties = appProperties;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(activeUserArgumentResolver);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> allowedOrigins = new ArrayList<>();
        if (appProperties.clientUrl() != null) {
            if (appProperties.clientUrl().backoffice() != null) {
                allowedOrigins.add(appProperties.clientUrl().backoffice());
            }
            if (appProperties.clientUrl().storefront() != null) {
                allowedOrigins.add(appProperties.clientUrl().storefront());
            }
        }
        if (allowedOrigins.isEmpty()) {
            allowedOrigins.add("http://localhost:5173");
            allowedOrigins.add("http://localhost:3000");
        }

        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(MAX_AGE_SECS);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}

