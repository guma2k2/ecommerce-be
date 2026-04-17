package com.yas.system.common.config;

import com.yas.system.common.security.ActiveUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ActiveUserArgumentResolver activeUserArgumentResolver;

    public WebMvcConfig(ActiveUserArgumentResolver activeUserArgumentResolver) {
        this.activeUserArgumentResolver = activeUserArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(activeUserArgumentResolver);
    }
}
