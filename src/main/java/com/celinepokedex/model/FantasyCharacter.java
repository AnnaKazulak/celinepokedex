package com.celinepokedex.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fantasy_characters")
public class FantasyCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Default constructor
    public FantasyCharacter() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructor with fields
    public FantasyCharacter(String prompt, String imageUrl) {
        this.prompt = prompt;
        this.imageUrl = imageUrl;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "FantasyCharacter{" +
                "id=" + id +
                ", prompt='" + prompt + '\'' +
                ", imageUrl='" + (imageUrl != null ? imageUrl.substring(0, Math.min(30, imageUrl.length())) + "..." : null) + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}