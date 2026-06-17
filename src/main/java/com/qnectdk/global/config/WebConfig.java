package com.qnectdk.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 로컬 저장소에 올린 프로필 이미지를 /files/** 로 서빙한다 (S3 전 임시).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String localBasePath;

    public WebConfig(@Value("${app.storage.local.base-path}") String localBasePath) {
        this.localBasePath = localBasePath;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(localBasePath).toAbsolutePath().toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler("/files/**").addResourceLocations(location);
    }
}
