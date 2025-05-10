package com.celinepokedex.service;

import com.celinepokedex.model.BaseAnimal;
import com.celinepokedex.model.CharacterTrait;
import com.celinepokedex.model.DominantColor;
import com.celinepokedex.model.ElementType;
import com.celinepokedex.model.StyleType;
import com.celinepokedex.util.PromptBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class ImageGenerationService {

    private static final Logger logger = Logger.getLogger(ImageGenerationService.class.getName());
    
    private final RestTemplate restTemplate;
    private final String huggingfaceToken;
    private final String apiUrl = "https://api-inference.huggingface.co/models/stabilityai/stable-diffusion-xl-base-1.0";

    public ImageGenerationService(@Value("${huggingface.token}") String huggingfaceToken) {
        this.restTemplate = new RestTemplate();
        this.huggingfaceToken = huggingfaceToken;
        logger.info("ImageGenerationService initialized with token: " + 
                    (huggingfaceToken != null ? huggingfaceToken.substring(0, 5) + "..." : "null"));
    }
    
    /**
     * Generates an image using user-selected character attributes (Legacy method for backward compatibility)
     * 
     * @param animal The base animal type for the character
     * @param element The elemental power of the character
     * @param style The art style to generate the character in
     * @param traits List of character traits to apply
     * @return Base64 encoded image data
     * @deprecated Use {@link #generateFantasyCharacterImage(BaseAnimal, ElementType, DominantColor, StyleType, List)} instead
     */
    @Deprecated
    public String generateFantasyCharacterImage(BaseAnimal animal, ElementType element, StyleType style, List<CharacterTrait> traits) {
        // Call the complete method with null for dominantColor
        return generateFantasyCharacterImage(animal, element, null, style, traits);
    }
    
    /**
     * Generates an image using user-selected character attributes including dominant color
     * 
     * @param animal The base animal type for the character
     * @param element The elemental power of the character
     * @param dominantColor The dominant color of the character (can be null)
     * @param style The art style to generate the character in
     * @param traits List of character traits to apply
     * @return Base64 encoded image data
     */
    public String generateFantasyCharacterImage(BaseAnimal animal, ElementType element, DominantColor dominantColor, StyleType style, List<CharacterTrait> traits) {
        // Validate required parameters
        if (animal == null) {
            throw new IllegalArgumentException("Base animal cannot be null");
        }
        if (element == null) {
            throw new IllegalArgumentException("Element type cannot be null");
        }
        if (style == null) {
            throw new IllegalArgumentException("Style type cannot be null");
        }
        
        // Build the prompt and generate the image
        String prompt = PromptBuilder.buildPrompt(animal, element, dominantColor, style, traits);
        logger.info("Generated prompt from attributes: " + prompt);
        return generateImage(prompt);
    }

    /**
     * Generates an image using the provided prompt text
     *
     * @param prompt The text prompt to generate an image from
     * @return Base64 encoded image data
     * @throws RuntimeException if image generation fails
     */
    public String generateImage(String prompt) {
        logger.info("Generating image for prompt: " + prompt);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + huggingfaceToken);
        headers.set("Accept", "image/png");
        
        logger.info("Authorization header set: Bearer " + 
                    (huggingfaceToken != null ? huggingfaceToken.substring(0, 5) + "..." : "null"));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("inputs", prompt);
        requestBody.put("parameters", Map.of(
            "negative_prompt", "blurry, bad quality, deformed, disfigured",
            "guidance_scale", 7.5,
            "num_inference_steps", 30
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            logger.info("Sending request to Hugging Face API: " + apiUrl);
            ResponseEntity<byte[]> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                byte[].class
            );
            
            logger.info("Response status: " + response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // In a production environment, you'd want to store this image somewhere
                // For this example, we're using a temporary base64 encoding
                String base64Image = java.util.Base64.getEncoder().encodeToString(response.getBody());
                logger.info("Image successfully generated and encoded");
                return "data:image/png;base64," + base64Image;
            } else {
                logger.warning("Unsuccessful response: " + response.getStatusCode());
                throw new RuntimeException("Failed to generate image: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            logger.severe("HTTP Client Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw new RuntimeException("Error from Hugging Face API (client error): " + e.getStatusCode() + " - " + e.getMessage());
        } catch (HttpServerErrorException e) {
            logger.severe("HTTP Server Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw new RuntimeException("Error from Hugging Face API (server error): " + e.getStatusCode() + " - " + e.getMessage());
        } catch (RestClientException e) {
            logger.severe("Rest Client Exception: " + e.getMessage());
            throw new RuntimeException("Error connecting to Hugging Face API: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error calling Hugging Face API: " + e.getMessage());
        }
    }
}