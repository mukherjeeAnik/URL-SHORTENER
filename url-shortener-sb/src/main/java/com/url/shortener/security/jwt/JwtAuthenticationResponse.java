package com.url.shortener.security.jwt;
import lombok.AllArgsConstructor;
import lombok.Data;

// Simple DTO — wraps the JWT token string returned to the client after successful login
// Response body: { "token": "eyJhbGci..." }
@Data           // Lombok: generates getter, setter, equals, hashCode, toString
@AllArgsConstructor // Lombok: generates constructor with token as argument
public class JwtAuthenticationResponse {
    private String token;
}
