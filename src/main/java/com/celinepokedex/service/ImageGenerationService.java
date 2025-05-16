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
    // Nutze das OpenJourney Modell
    private final String apiUrl = "https://api-inference.huggingface.co/models/prompthero/openjourney";
    private final CloudinaryService cloudinaryService;

    // Timeouts
    private static final int CONNECTION_TIMEOUT = 60000; // 60 Sekunden
    private static final int READ_TIMEOUT = 120000;      // 120 Sekunden

    
    public ImageGenerationService(@Value("${huggingface.token}") String huggingfaceToken, 
                                 CloudinaryService cloudinaryService) {
        this.restTemplate = new RestTemplate();
        this.cloudinaryService = cloudinaryService;
        restTemplate.setRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory());
        ((org.springframework.http.client.SimpleClientHttpRequestFactory) restTemplate
            .getRequestFactory()).setConnectTimeout(CONNECTION_TIMEOUT);
        ((org.springframework.http.client.SimpleClientHttpRequestFactory) restTemplate
            .getRequestFactory()).setReadTimeout(READ_TIMEOUT);

        this.huggingfaceToken = huggingfaceToken;
        logger.info("ImageGenerationService initialized with token: " + 
                    (huggingfaceToken != null ? huggingfaceToken.substring(0, 5) + "..." : "null") + 
                    " and timeouts: connect=" + CONNECTION_TIMEOUT + "ms, read=" + READ_TIMEOUT + "ms");
    }

    public String generateFantasyCharacterImage(BaseAnimal animal, ElementType element, DominantColor dominantColor, StyleType style, List<CharacterTrait> traits) {
        if (animal == null) throw new IllegalArgumentException("Base animal cannot be null");
        if (element == null) throw new IllegalArgumentException("Element type cannot be null");
        if (style == null) throw new IllegalArgumentException("Style type cannot be null");
        String prompt = PromptBuilder.buildPrompt(animal, element, dominantColor, style, traits);
        logger.info("Generated prompt from attributes: " + prompt);
        return generateImage(prompt);
    }

    public String generateImage(String prompt) {
        logger.info("Generating image for prompt: " + prompt);

        // OpenJourney erwartet "mdjrny-v4 style," im Prompt!
        String journeyPrompt = "mdjrny-v4 style, " + prompt;

        // Starte mit 384x384, fallback auf 256x256 falls CUDA error
        int[] sizes = {384, 256};

        Exception lastException = null;
        for (int attempt = 0; attempt < sizes.length; attempt++) {
            int size = sizes[attempt];
            logger.info("Versuch " + (attempt+1) + " mit Bildgröße " + size + "x" + size);
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + huggingfaceToken);

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("height", size);
                parameters.put("width", size);
                parameters.put("num_inference_steps", 20);
                parameters.put("guidance_scale", 7.5);
                parameters.put("negative_prompt", "blurry, bad quality, deformed, disfigured");
                // Optional: parameters.put("return_full_object", false);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("inputs", journeyPrompt);
                requestBody.put("parameters", parameters);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

                logger.info("Sending request to Hugging Face API: " + apiUrl);
                ResponseEntity<byte[]> response = restTemplate.exchange(
                        apiUrl,
                        HttpMethod.POST,
                        entity,
                        byte[].class
                );

                logger.info("Response status: " + response.getStatusCode());

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    String imageUrl = cloudinaryService.uploadImage(response.getBody());
                    logger.info("Image successfully generated and uploaded to Cloudinary");
                    return imageUrl;
                } else {
                    logger.warning("Unsuccessful response: " + response.getStatusCode());
                    lastException = new RuntimeException("Failed to generate image: " + response.getStatusCode());
                }
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                logger.warning("HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
                lastException = e;
                // CUDA/memory Fehler? Fallback!
                if ((e.getResponseBodyAsString() != null && 
                    (e.getResponseBodyAsString().contains("CUDA") || 
                    e.getResponseBodyAsString().toLowerCase().contains("memory"))) && attempt + 1 < sizes.length) {
                    logger.warning("GPU/Memory Problem - versuche kleinere Bildgröße...");
                    try { Thread.sleep(1200); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    continue;
                } else {
                    break;
                }
            } catch (RestClientException e) {
                logger.warning("Rest Client Exception: " + e.getMessage());
                lastException = e;
                break;
            } catch (Exception e) {
                logger.warning("Unexpected error: " + e.getMessage());
                e.printStackTrace();
                lastException = e;
                break;
            }
        }
        logger.severe("Alle Versuche fehlgeschlagen. Letzter Fehler: " + (lastException != null ? lastException.getMessage() : "Unknown error"));
        if (lastException != null) {
            if (lastException instanceof RuntimeException) throw (RuntimeException) lastException;
            else throw new RuntimeException("Error calling Hugging Face API: " + lastException.getMessage(), lastException);
        } else {
            throw new RuntimeException("Failed to generate image after all attempts.");
        }
    }

    public String generateImageFromPrompt(String prompt) {
        logger.info("Generating image from user prompt: " + prompt);
        String enhancedPrompt = "A high quality, detailed fantasy character: " + prompt + ", digital art, concept art, vibrant colors, highly detailed";
        return generateImage(enhancedPrompt);
    }
}
