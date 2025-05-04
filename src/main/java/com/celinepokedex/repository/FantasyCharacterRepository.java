package com.celinepokedex.repository;

import com.celinepokedex.model.FantasyCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FantasyCharacterRepository extends JpaRepository<FantasyCharacter, Long> {
    
    /**
     * Find fantasy characters that contain the query string in their prompts
     */
    @Query("SELECT f FROM FantasyCharacter f WHERE LOWER(f.prompt) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY f.createdAt DESC")
    List<FantasyCharacter> findByPromptContaining(@Param("query") String query);
    
    /**
     * Get all characters ordered by creation date (newest first)
     */
    List<FantasyCharacter> findAllByOrderByCreatedAtDesc();
}