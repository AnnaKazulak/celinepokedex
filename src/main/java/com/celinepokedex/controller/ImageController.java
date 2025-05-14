package com.celinepokedex.controller;

import com.celinepokedex.service.CloudinaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private static final Logger logger = Logger.getLogger(ImageController.class.getName());
    private final CloudinaryService cloudinaryService;

    public ImageController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
        logger.info("ImageController initialized with CloudinaryService");
    }

    /**
     * Handle image upload and return the URL to the Cloudinary-hosted image
     * POST /api/images/upload
     * 
     * @param image The image file to upload
     * @return The URL to the saved image in Cloudinary
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("image") MultipartFile image) {
        if (image.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Please select a file to upload");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Read the image data
            byte[] imageData = image.getBytes();
            
            // Upload to Cloudinary
            String imageUrl = cloudinaryService.uploadImage(imageData);
            
            // Return the Cloudinary image URL
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("filename", image.getOriginalFilename());
            response.put("size", String.valueOf(image.getSize()));
            
            logger.info("Image successfully uploaded to Cloudinary: " + imageUrl);
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            logger.severe("Failed to upload image: " + e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete an image from Cloudinary
     * DELETE /api/images
     * 
     * @param imageUrl The URL of the image to delete
     * @return Success or error message
     */
    @DeleteMapping
    public ResponseEntity<Map<String, String>> deleteImage(@RequestParam("url") String imageUrl) {
        Map<String, String> response = new HashMap<>();
        
        if (imageUrl == null || imageUrl.isEmpty()) {
            response.put("error", "Image URL is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        boolean deleted = cloudinaryService.deleteImage(imageUrl);
        
        if (deleted) {
            response.put("message", "Image successfully deleted");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Failed to delete image");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}