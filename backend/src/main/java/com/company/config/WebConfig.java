package com.company.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String userDir = System.getProperty("user.dir");
        File baseDir = new File(userDir);
        String uploadPath;
        if (baseDir.getName().equals("backend")) {
            uploadPath = new File(baseDir, "uploads").getAbsolutePath();
        } else {
            uploadPath = new File(new File(baseDir, "backend"), "uploads").getAbsolutePath();
        }

        String resourceLocation = "file:" + uploadPath + File.separator;
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);

        log.info("映射静态资源路径: /uploads/** -> {}", resourceLocation);
    }
}
