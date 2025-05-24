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
        // Only allow admin login
        if ("admin".equals(username)) {
            Optional<User> adminUser = userRepository.findByUsername("admin");
            if (adminUser.isPresent() && passwordEncoder.matches(password, adminUser.get().getPassword())) {
                return createTokenResponse(username, "admin");
            }
        }
        throw new RuntimeException("Invalid username or password");
    }

    // Registrierung abschalten oder mit Exception blockieren
    public Map<String, String> register(String username, String password) {
        throw new RuntimeException("Registration is disabled. Only admin login is allowed.");
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
