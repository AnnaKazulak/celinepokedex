package com.celinepokedex.service;

import com.celinepokedex.model.User;
import com.celinepokedex.repository.UserRepository;
import com.celinepokedex.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Autowired
    public AuthService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @PostConstruct
    public void init() {
        // Create admin user on startup if it doesn't exist
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("admin");
            userRepository.save(admin);
        }
    }
    
    public Map<String, String> login(String username, String password) {
        // Special case for admin
        if ("admin".equals(username)) {
            Optional<User> adminUser = userRepository.findByUsername("admin");
            if (adminUser.isPresent() && passwordEncoder.matches(password, adminUser.get().getPassword())) {
                return createTokenResponse(username, "admin");
            }
        }
        
        // For regular users
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            return createTokenResponse(username, user.get().getRole());
        }
        
        throw new RuntimeException("Invalid username or password");
    }
    
    public Map<String, String> register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole("user");  // Default role is user
        userRepository.save(newUser);
        
        return createTokenResponse(username, "user");
    }
    
    private Map<String, String> createTokenResponse(String username, String role) {
        String token = jwtTokenProvider.generateToken(username, role);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("username", username);
        response.put("role", role);
        return response;
    }
    
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
    
    public String getUsernameFromToken(String token) {
        return jwtTokenProvider.getUsernameFromToken(token);
    }
    
    public String getRoleFromToken(String token) {
        return jwtTokenProvider.getRoleFromToken(token);
    }
}
