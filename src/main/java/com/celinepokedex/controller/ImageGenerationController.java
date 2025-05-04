package com.celinepokedex.controller;

import com.celinepokedex.service.ImageGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/images")
public class ImageGenerationController {

    private static final Logger logger = Logger.getLogger(ImageGenerationController.class.getName());

    @Autowired
    private ImageGenerationService imageGenerationService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateImage(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        logger.info("Received image generation request with prompt: " + prompt);
        
        if (prompt == null || prompt.trim().isEmpty()) {
            logger.warning("Empty prompt received");
            Map<String, String> errorResponse = Map.of(
                "error", "Prompt cannot be empty"
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            String imageUrl = imageGenerationService.generateImage(prompt);
            
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("prompt", prompt);
            
            logger.info("Successfully generated image");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error generating image: " + e.getMessage());
            Map<String, String> errorResponse = Map.of(
                "error", "Failed to generate image: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}