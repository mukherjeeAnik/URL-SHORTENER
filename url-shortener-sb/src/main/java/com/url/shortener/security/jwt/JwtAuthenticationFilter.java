package com.url.shortener.security.jwt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

// JWT filter — runs once per request, before Spring Security's auth checks
// Extracts JWT from header → validates → loads user → sets authentication in SecurityContext
// Extends OncePerRequestFilter to guarantee single execution per request
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtTokenProvider; // handles JWT parsing, validation, and claims extraction

    @Autowired
    private UserDetailsService userDetailsService; // loads UserDetails from DB by username

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = jwtTokenProvider.getJwtFromHeader(request); // extract token from "Authorization: Bearer <token>"

            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {

                String username = jwtTokenProvider.getUserNameFromJwtToken(jwt); // decode username from JWT claims
                UserDetails userDetails = userDetailsService.loadUserByUsername(username); // fetch user + roles from DB

                if (userDetails != null) {
                    // Build authentication object — credentials = null (already verified via JWT, no password needed)
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    // Attach request metadata (IP, session ID) to the authentication object
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Register authentication in SecurityContext — makes Principal available in controllers
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            // Silently skip auth on invalid/expired token — request continues as unauthenticated
            // Protected routes will then be rejected by Spring Security's access control
            e.printStackTrace();
        }

        filterChain.doFilter(request, response); // always pass request down the filter chain
    }
}
