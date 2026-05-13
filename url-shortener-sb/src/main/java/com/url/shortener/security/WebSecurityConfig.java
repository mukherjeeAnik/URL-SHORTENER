package com.url.shortener.security;

import com.url.shortener.security.jwt.JwtAuthenticationFilter;
import com.url.shortener.service.UserDetailsServiceImpl;

import lombok.AllArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/*
 * Marks this class as a Spring Security configuration class.
 * Spring automatically detects and applies these security settings at startup.
 */
@Configuration

/*
 * Enables Spring Security's web security support.
 * This activates the security filter chain for incoming HTTP requests.
 */
@EnableWebSecurity

/*
 * Enables method-level security annotations like:
 * @PreAuthorize
 * @PostAuthorize
 * @Secured
 */
@EnableMethodSecurity

/*
 * Lombok automatically generates a constructor for all final fields.
 * This is cleaner than using @Autowired field injection.
 */
@AllArgsConstructor
public class WebSecurityConfig {

    /*
     * Custom implementation of Spring Security's UserDetailsService.
     * Responsible for loading user information from the database.
     */
    private final UserDetailsServiceImpl userDetailsService;

    /*
     * Registers the custom JWT authentication filter as a Spring Bean.
     *
     * This filter intercepts every request,
     * extracts the JWT token,
     * validates it,
     * and sets authentication in the SecurityContext.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    /*
     * Password encoder bean using BCrypt hashing algorithm.
     *
     * BCrypt automatically salts passwords and is highly recommended
     * for secure password storage.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * Authentication provider responsible for:
     * 1. Fetching user details from the database
     * 2. Validating passwords using PasswordEncoder
     *
     * DaoAuthenticationProvider is commonly used
     * for username/password authentication.
     */

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration){
        return authenticationConfiguration.getAuthenticationManager();
    }



    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        // Constructor injection is required in newer Spring Security versions
        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider(userDetailsService);

        // Configure password verification strategy
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }



    /*
     * Main Security Filter Chain configuration.
     *
     * Defines:
     * - Which routes are public/private
     * - JWT filter placement
     * - Authentication provider
     * - CSRF behavior
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http

                /*
                 * Disable CSRF because this application uses JWT authentication.
                 *
                 * CSRF protection is mainly needed for session/cookie-based auth.
                 */
                .csrf(AbstractHttpConfigurer::disable)

                /*
                 * Configure endpoint authorization rules.
                 */
                .authorizeHttpRequests(auth -> auth

                        /*
                         * Authentication endpoints are public.
                         *
                         * Examples:
                         * /api/auth/login
                         * /api/auth/register
                         */
                        .requestMatchers("/api/auth/**").permitAll()

                        /*
                         * URL management endpoints require authentication.
                         */
                        .requestMatchers("/api/urls/**").authenticated()

                        /*
                         * Public short URLs should be accessible without login.
                         *
                         * Example:
                         * localhost:8080/abc123
                         */
                        .requestMatchers("/{shortUrl}").permitAll()

                        /*
                         * Any other endpoint requires authentication by default.
                         */
                        .anyRequest().authenticated()
                );

        /*
         * Register the custom authentication provider.
         */
        http.authenticationProvider(authenticationProvider());

        /*
         * Add JWT filter before Spring's default
         * UsernamePasswordAuthenticationFilter.
         *
         * This ensures JWT validation happens early in the chain.
         */
        http.addFilterBefore(
                jwtAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter.class
        );

        /*
         * Build and return the configured security filter chain.
         */
        return http.build();
    }


}