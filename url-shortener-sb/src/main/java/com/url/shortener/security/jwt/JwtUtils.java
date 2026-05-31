package com.url.shortener.security.jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

// Core JWT utility — handles token generation, parsing, and validation
// Secret and expiry are externalized in application.properties via @Value
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret; // Base64-encoded HMAC-SHA secret key (keep in env vars, never hardcode)

    @Value("${jwt.expiration}")
    private int jwtExpirationMs; // token lifetime in milliseconds (e.g. 86400000 = 24 hours)

    // Extracts JWT from "Authorization: Bearer <token>" header
    // Returns raw token string, or null if header is missing/malformed
    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // strip "Bearer " prefix (7 chars) to get raw token
        }
        return null;
    }

    // Builds and signs a JWT for the authenticated user
    // Payload: subject = username, custom claim "roles" = comma-separated roles (e.g. "ROLE_USER")
    public String generateToken(UserDetails userDetails) {
        String username = userDetails.getUsername();
        String roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.joining(",")); // e.g. "ROLE_USER" or "ROLE_USER,ROLE_ADMIN"

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)        // embedded in token — no DB call needed to check roles
                .issuedAt(new Date())          // iat claim
                .expiration(new Date(new Date().getTime() + jwtExpirationMs)) // exp claim
                .signWith(key())               // signs with HMAC-SHA key — tamper-proof
                .compact();                    // serializes to "header.payload.signature" string
    }

    // Parses the token and extracts the username from the "sub" (subject) claim
    // Throws if token is invalid — always call validateToken() first
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key()) // verify signature before reading claims
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();                 // returns the username set during generateToken()
    }

    // Decodes the Base64 secret and builds an HMAC-SHA key for signing/verification
    // Private — only used internally by generateToken() and validateToken()
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Returns true if token signature is valid and not expired
    // Any JwtException (expired, malformed, wrong signature) is wrapped and rethrown
    // Note: rethrowing as RuntimeException loses the specific error type — consider returning false instead
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(authToken); // throws if invalid or expired
            return true;
        } catch (JwtException e) {
            throw new RuntimeException(e); // covers expired, malformed, wrong signature
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e); // covers null/empty token string
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
