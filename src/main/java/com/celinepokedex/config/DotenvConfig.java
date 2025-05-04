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
            String token = dotenv.get("HUGGINGFACE_API_TOKEN");
            if (token != null && !token.isEmpty()) {
                System.setProperty("HUGGINGFACE_API_TOKEN", token);
                logger.info("Successfully loaded Hugging Face API token from .env file");
            } else {
                logger.warning("No Hugging Face API token found in .env file");
            }
        } catch (Exception e) {
            logger.severe("Failed to load .env file: " + e.getMessage());
        }
    }
}