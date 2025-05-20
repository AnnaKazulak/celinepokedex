package com.celinepokedex.config;

import com.celinepokedex.service.ImageMigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * Runs tasks at application startup
 */
@Component
public class ApplicationStartupRunner implements CommandLineRunner {
    
    private static final Logger logger = Logger.getLogger(ApplicationStartupRunner.class.getName());
    
    private final ImageMigrationService imageMigrationService;
    
    @Autowired
    public ApplicationStartupRunner(ImageMigrationService imageMigrationService) {
        this.imageMigrationService = imageMigrationService;
    }
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Application started, running startup tasks...");
        
        // Run image migration to ensure all images are in Cloudinary
        imageMigrationService.migrateAllImages();
    }
}