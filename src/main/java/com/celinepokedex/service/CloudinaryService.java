package com.celinepokedex.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class CloudinaryService {

    private static final Logger logger = Logger.getLogger(CloudinaryService.class.getName());
    
    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret,
            "secure", true
        ));
        
        logger.info("CloudinaryService initialized with cloud name: " + cloudName);
    }
    
    /**
     * Uploads an image to Cloudinary and returns the public URL
     *
     * @param imageData The image data as byte array
     * @return The public URL of the uploaded image
     * @throws RuntimeException if upload fails
     */
    public String uploadImage(byte[] imageData) {
        try {
            logger.info("Uploading image to Cloudinary");
            
            // Generate a unique public ID for the image
            String publicId = "fantasy_character_" + UUID.randomUUID().toString();
            
            // Upload parameters
            Map<String, Object> params = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", "fantasy_characters",
                "overwrite", true,
                "resource_type", "image"
            );
            
            // Upload image and get response
            Map<?, ?> uploadResult = cloudinary.uploader().upload(imageData, params);
            
            // Get the secure URL from the response
            String imageUrl = (String) uploadResult.get("secure_url");
            
            logger.info("Image successfully uploaded to Cloudinary: " + imageUrl);
            return imageUrl;
            
        } catch (IOException e) {
            logger.severe("Error uploading image to Cloudinary: " + e.getMessage());
            throw new RuntimeException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }
    
    /**
     * Deletes an image from Cloudinary by URL
     *
     * @param imageUrl The URL of the image to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteImage(String imageUrl) {
        try {
            // Extract public ID from URL
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId == null) {
                logger.warning("Could not extract public ID from URL: " + imageUrl);
                return false;
            }
            
            logger.info("Deleting image from Cloudinary with public ID: " + publicId);
            
            // Delete the image
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            
            // Check if deletion was successful
            String status = (String) result.get("result");
            boolean success = "ok".equals(status);
            
            logger.info("Image deletion " + (success ? "successful" : "failed") + ": " + status);
            return success;
            
        } catch (IOException e) {
            logger.severe("Error deleting image from Cloudinary: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Extracts the public ID from a Cloudinary URL
     *
     * @param url The Cloudinary URL
     * @return The public ID or null if it couldn't be extracted
     */
    private String extractPublicIdFromUrl(String url) {
        if (url == null || !url.contains("cloudinary.com")) {
            return null;
        }
        
        try {
            // Example URL: https://res.cloudinary.com/cloud-name/image/upload/v1234567890/fantasy_characters/fantasy_character_uuid.jpg
            String[] parts = url.split("/upload/");
            if (parts.length < 2) return null;
            
            String path = parts[1];
            // Remove version number if present
            if (path.contains("/v")) {
                path = path.replaceFirst("v\\d+/", "");
            }
            
            // Remove file extension
            int extensionIndex = path.lastIndexOf(".");
            if (extensionIndex > 0) {
                path = path.substring(0, extensionIndex);
            }
            
            return path;
            
        } catch (Exception e) {
            logger.warning("Failed to extract public ID from URL: " + e.getMessage());
            return null;
        }
    }
}