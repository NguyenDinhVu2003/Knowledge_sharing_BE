package com.company.knowledge_sharing_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve files from uploads folder at project root level
        String uploadPath = Paths.get("uploads").toAbsolutePath().toString();

        // Map /*.jpg, /*.png, /*.pdf, etc. to uploads folder
        registry.addResourceHandler("/*.jpg", "/*.jpeg", "/*.png", "/*.gif", "/*.pdf", "/*.docx", "/*.doc")
                .addResourceLocations("file:///" + uploadPath + "/")
                .setCachePeriod(3600); // Cache for 1 hour
    }
}

