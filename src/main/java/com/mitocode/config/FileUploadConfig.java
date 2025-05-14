package com.mitocode.config;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadConfig {

    private String uploadDir;

    @PostConstruct
    public void adjustUploadDir() {
        String osName = System.getProperty("os.name").toLowerCase();
        System.out.println("Sistema operativo detectado: " + osName);

        if (osName.contains("win")) {
            this.uploadDir = "C:/uploads/";
        } else if (osName.contains("nux") || osName.contains("nix") || osName.contains("mac")) {
            this.uploadDir = "/opt/springboot/uploads/";
        } else {
            throw new RuntimeException("Sistema operativo no soportado: " + osName);
        }

        System.out.println("Directorio de subida configurado en: " + this.uploadDir);
    }
}
