package com.celinepokedex.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import java.util.logging.Logger;

@Configuration
public class DotenvConfig {

    private static final Logger logger = Logger.getLogger(DotenvConfig.class.getName());

    @PostConstruct
    public void loadEnv() {
        try {
            // Load .env file from the project root
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            
            // Set Hugging Face token as a system property
            String huggingFaceToken = dotenv.get("HUGGINGFACE_API_TOKEN");
            if (huggingFaceToken != null && !huggingFaceToken.isEmpty()) {
                System.setProperty("HUGGINGFACE_API_TOKEN", huggingFaceToken);
                logger.info("Successfully loaded Hugging Face API token from .env file");
            } else {
                logger.warning("No Hugging Face API token found in .env file");
            }
            
            // Set Cloudinary credentials as system properties
            String cloudName = dotenv.get("CLOUDINARY_CLOUD_NAME");
            String apiKey = dotenv.get("CLOUDINARY_API_KEY");
            String apiSecret = dotenv.get("CLOUDINARY_API_SECRET");
            
            if (cloudName != null && apiKey != null && apiSecret != null) {
                System.setProperty("CLOUDINARY_CLOUD_NAME", cloudName);
                System.setProperty("CLOUDINARY_API_KEY", apiKey);
                System.setProperty("CLOUDINARY_API_SECRET", apiSecret);
                logger.info("Successfully loaded Cloudinary credentials from .env file");
            } else {
                logger.warning("One or more Cloudinary credentials missing in .env file");
            }
        } catch (Exception e) {
            logger.severe("Failed to load .env file: " + e.getMessage());
        }
    }
}