package com.celinepokedex.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    // Store images in a system temp directory by default
    // This can be overridden with application property
    @Value("${app.upload.dir:${java.io.tmpdir}}")
    private String uploadDir;

    /**
     * Handle image upload and return the URL to the saved image
     * POST /api/images/upload
     * 
     * @param image The image file to upload
     * @return The URL to the saved image
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("image") MultipartFile image) {
        if (image.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Please select a file to upload");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Create upload directory if it doesn't exist
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // Generate a unique filename to avoid conflicts
            String originalFilename = image.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // Save the file
            Path filePath = Paths.get(uploadDir, filename);
            Files.write(filePath, image.getBytes());

            // Return the image URL
            // In a real-world scenario, this would be a URL to your static file server or CDN
            String imageUrl = "/api/images/" + filename;
            
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("filename", filename);
            response.put("size", String.valueOf(image.getSize()));
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get an image by filename
     * GET /api/images/{filename}
     * 
     * @param filename The name of the file to retrieve
     * @return The image file
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir, filename);
            byte[] image = Files.readAllBytes(filePath);
            
            // Determine content type based on file extension
            String contentType = "image/jpeg"; // default
            if (filename.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (filename.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            } else if (filename.toLowerCase().endsWith(".webp")) {
                contentType = "image/webp";
            }
            
            return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .body(image);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}