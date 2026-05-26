package com.hmbrandt.job_management_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${application.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        File file = new File(uploadDir);
        String absolutePath = file.getAbsolutePath();

        // 1. Convertimos TODAS las barras invertidas de Windows (\) a barras diagonales web (/)
        String cleanPath = absolutePath.replace("\\", "/");

        // 2. Formateamos el protocolo file:/// usando la ruta limpia
        String resourceLocation = cleanPath.startsWith("/") ? "file:" + cleanPath : "file:///" + cleanPath;

        // 3. Nos aseguramos de que termine con una barra diagonal final
        if (!resourceLocation.endsWith("/")) {
            resourceLocation += "/";
        }

        // NUESTRO ESPÍA CORREGIDO
//        System.out.println("\n=======================================================");
//        System.out.println("NUEVA RUTA LIMPIA EN SPRING: " + resourceLocation);
//        System.out.println("=======================================================\n");

        registry.addResourceHandler("/uploads/signatures/**")
                .addResourceLocations(resourceLocation);
    }
}
