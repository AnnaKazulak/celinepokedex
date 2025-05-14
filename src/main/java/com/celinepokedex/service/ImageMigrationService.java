package com.celinepokedex.service;

import com.celinepokedex.model.FantasyCharacter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service to handle migration of images from base64 or local paths to Cloudinary URLs
 */
@Service
public class ImageMigrationService {
    
    private static final Logger logger = Logger.getLogger(ImageMigrationService.class.getName());
    
    private final CloudinaryService cloudinaryService;
    private final FantasyCharacterService fantasyCharacterService;
    private final RestTemplate restTemplate;
    
    // Default fallback image for missing images
    private static final String DEFAULT_IMAGE_PATH = "/Users/annakazulak/Desktop/celinepokedex/src/main/resources/static/images/default-fantasy-character.png";
    
    @Autowired
    public ImageMigrationService(CloudinaryService cloudinaryService, 
                                FantasyCharacterService fantasyCharacterService) {
        this.cloudinaryService = cloudinaryService;
        this.fantasyCharacterService = fantasyCharacterService;
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Migrates all fantasy character images to Cloudinary
     * This should be called at application startup
     */
    @Transactional
    public void migrateAllImages() {
        List<FantasyCharacter> characters = fantasyCharacterService.getAllFantasyCharacters();
        int migratedCount = 0;
        int errorCount = 0;
        
        logger.info("Starting migration of " + characters.size() + " fantasy character images");
        
        for (FantasyCharacter character : characters) {
            if (character.getImageUrl() == null || needsMigration(character.getImageUrl())) {
                try {
                    String oldImageUrl = character.getImageUrl();
                    String newImageUrl = migrateImage(oldImageUrl, character.getId());
                    
                    if (newImageUrl != null && !newImageUrl.isEmpty()) {
                        character.setImageUrl(newImageUrl);
                        fantasyCharacterService.saveFantasyCharacter(character);
                        migratedCount++;
                        logger.info("Successfully migrated image for character ID: " + character.getId());
                    } else {
                        // Use a default image as a fallback
                        String defaultImageUrl = uploadDefaultImage();
                        if (defaultImageUrl != null) {
                            character.setImageUrl(defaultImageUrl);
                            fantasyCharacterService.saveFantasyCharacter(character);
                            logger.warning("Using default image for character ID: " + character.getId());
                        }
                        errorCount++;
                    }
                } catch (Exception e) {
                    logger.severe("Error migrating image for character ID " + character.getId() + ": " + e.getMessage());
                    errorCount++;
                }
            }
        }
        
        logger.info("Image migration completed. Migrated " + migratedCount + " out of " + characters.size() + 
                    " images. Errors: " + errorCount);
    }
    
    /**
     * Determines if an image URL needs to be migrated to Cloudinary
     */
    private boolean needsMigration(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return true;
        }
        
        // Check if it's already a Cloudinary URL
        if (imageUrl.contains("cloudinary.com")) {
            return false;
        }
        
        // Check if it's a base64 image or a local path
        return imageUrl.startsWith("data:") || 
               imageUrl.startsWith("/api/") || 
               imageUrl.startsWith("http://localhost");
    }
    
    /**
     * Migrates a single image to Cloudinary
     */
    private String migrateImage(String oldImageUrl, Long characterId) {
        try {
            if (oldImageUrl == null || oldImageUrl.isEmpty()) {
                logger.warning("Empty image URL for character ID: " + characterId);
                return uploadDefaultImage();
            }
            
            if (oldImageUrl.startsWith("data:image")) {
                // Handle base64 image
                String base64Data = oldImageUrl.substring(oldImageUrl.indexOf(",") + 1);
                byte[] imageData = Base64Utils.decodeFromString(base64Data);
                return cloudinaryService.uploadImage(imageData);
            } else if (oldImageUrl.startsWith("/api/") || oldImageUrl.contains("localhost")) {
                // For missing local files, use a placeholder image
                logger.warning("Local path cannot be migrated directly: " + oldImageUrl);
                return uploadDefaultImage();
            } else {
                logger.warning("Unknown image format: " + oldImageUrl);
                return uploadDefaultImage();
            }
        } catch (Exception e) {
            logger.severe("Error during image migration: " + e.getMessage());
            try {
                return uploadDefaultImage();
            } catch (Exception ex) {
                logger.severe("Failed to upload default image: " + ex.getMessage());
                return null;
            }
        }
    }
    
    /**
     * Uploads a default image to Cloudinary for cases where the original image can't be migrated
     */
    private String uploadDefaultImage() throws IOException {
        try {
            // First check if we have a static default image
            byte[] defaultImageData = getDefaultImageData();
            if (defaultImageData != null) {
                return cloudinaryService.uploadImage(defaultImageData);
            }
            
            // If no default image is available, generate a simple placeholder
            // This creates a simple colored image with text
            return "https://res.cloudinary.com/dsfjwudqk/image/upload/v1620000000/fantasy_characters/default_placeholder.png";
        } catch (Exception e) {
            logger.severe("Error creating default image: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Gets the default image data
     */
    private byte[] getDefaultImageData() {
        try {
            return Files.readAllBytes(Paths.get(DEFAULT_IMAGE_PATH));
        } catch (IOException e) {
            logger.warning("Could not read default image: " + e.getMessage());
            
            // Create a simple base64 encoded placeholder image (1x1 transparent pixel)
            String base64Placeholder = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=";
            return Base64Utils.decodeFromString(base64Placeholder);
        }
    }
}