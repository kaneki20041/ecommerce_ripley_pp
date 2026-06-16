package com.proyecto.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Convierte la ruta de texto en una ruta absoluta del sistema
        String absolutePath = Paths.get(uploadDir).toFile().getAbsolutePath();

        // Le dice a Spring: "Si alguien entra a http://localhost:8080/uploads/foto.jpg, 
        // busca ese archivo en la carpeta física de la PC"
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath + "/");
    }
}