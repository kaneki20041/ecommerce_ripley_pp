package com.proyecto.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/upload")
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')") // Solo personal autorizado puede subir fotos
public class FileUploadController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping("/image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Verificamos que la carpeta exista. Si no, la creamos.
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 2. Generamos un nombre único para que no choquen archivos con el mismo nombre
            // Ej: "polo.jpg" se convierte en "8f3a2b1c-polo.jpg"
            String originalFileName = file.getOriginalFilename();
            String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;

            // 3. Ruta completa donde se va a guardar
            Path filePath = Paths.get(uploadDir, uniqueFileName);

            // 4. Copiamos el archivo que mandó Angular a nuestra carpeta física
            Files.copy(file.getInputStream(), filePath);

            // 5. Construimos la URL pública que Angular va a guardar en la base de datos
            // OJO: En producción, cambiarías "http://localhost:8080" por tu dominio "https://minerva.com"
            String imageUrl = "http://localhost:8080/uploads/" + uniqueFileName;

            // Devolvemos el link cortito a Angular
            return ResponseEntity.ok(imageUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar la imagen");
        }
    }
}