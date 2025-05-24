package com.celinepokedex.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/characters/save", "/api/characters/save-generated", "/api/characters/generate*", "/api/characters/generate-description", "/api/characters/generate-name", "/api/characters/generate-prompt").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/characters/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/characters/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/characters/**").permitAll()
                .anyRequest().permitAll()
            )
            .httpBasic(httpBasic -> httpBasic.disable()); // <-- KEIN Basic Auth!
        return http.build();
    }

    // Optional, wenn du irgendwo einen AuthenticationManager brauchst
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
