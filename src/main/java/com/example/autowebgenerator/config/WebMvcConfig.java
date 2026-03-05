package com.example.autowebgenerator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves AI-generated and deployed website files as static resources.
 *
 * With context-path /api these are reachable at:
 *   /api/app-output/{appId}/index.html   — live preview after generation
 *   /api/deploy/{deployKey}/index.html   — publicly deployed apps
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String base = System.getProperty("user.dir").replace("\\", "/");
        registry.addResourceHandler("/app-output/**")
                .addResourceLocations("file:" + base + "/tmp/code_output/");

        registry.addResourceHandler("/deploy/**")
                .addResourceLocations("file:" + base + "/tmp/code_deploy/");

        registry.addResourceHandler("/covers/**")
                .addResourceLocations("file:" + base + "/tmp/covers/");
    }
}
