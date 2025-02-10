package com.gt.ssm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig extends WebMvcAutoConfiguration implements WebMvcConfigurer {

    private final Logger log = LoggerFactory.getLogger(WebConfig.class);

    private final String allowedOrigin;

    @Autowired
    public WebConfig(@Value("${server.cors.allowedOrigin:}") String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/app/**").setViewName("forward:/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (allowedOrigin != null && !allowedOrigin.isBlank()) {
            registry.addMapping("/graphql**").allowedOrigins(allowedOrigin);
        }
    }
}
