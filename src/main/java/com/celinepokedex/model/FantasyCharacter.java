package com.celinepokedex.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fantasy_characters")
public class FantasyCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;
    
    private String baseAnimal;
    
    private String elementType;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String dominantColor;
    
    @Column(name = "is_public")
    private Boolean isPublic = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_by_role", nullable = false)
    private String createdByRole;


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
    
    // Constructor with name
    public FantasyCharacter(String name, String prompt, String imageUrl) {
        this.name = name;
        this.prompt = prompt;
        this.imageUrl = imageUrl;
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor with all fields including baseAnimal and elementType
    public FantasyCharacter(String name, String prompt, String imageUrl, String baseAnimal, String elementType, String createdBy) {
        this.name = name;
        this.prompt = prompt;
        this.imageUrl = imageUrl;
        this.baseAnimal = baseAnimal;
        this.elementType = elementType;
        this.createdAt = LocalDateTime.now();
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    
    public String getBaseAnimal() {
        return baseAnimal;
    }
    
    public void setBaseAnimal(String baseAnimal) {
        this.baseAnimal = baseAnimal;
    }
    
    public String getElementType() {
        return elementType;
    }
    
    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDominantColor() {
        return dominantColor;
    }
    
    public void setDominantColor(String dominantColor) {
        this.dominantColor = dominantColor;
    }
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getCreatedBy() {
    return createdBy;
    }

    public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
    }
    
    public String getCreatedByRole() {
    return createdByRole;
    }

    public void setCreatedByRole(String createdByRole) {
    this.createdByRole = createdByRole;
}


    @Override
    public String toString() {
        return "FantasyCharacter{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", prompt='" + prompt + '\'' +
                ", imageUrl='" + (imageUrl != null ? imageUrl.substring(0, Math.min(30, imageUrl.length())) + "..." : null) + '\'' +
                ", baseAnimal='" + baseAnimal + '\'' +
                ", elementType='" + elementType + '\'' +
                ", description='" + description + '\'' +
                ", isPublic=" + isPublic +
                ", createdAt=" + createdAt +
                ", createdBy='" + createdBy + '\'' +
                '}';
    }
}