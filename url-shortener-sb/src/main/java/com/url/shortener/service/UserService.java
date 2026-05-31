package com.url.shortener.service;
import com.url.shortener.dtos.LoginRequest;
import com.url.shortener.models.User;
import com.url.shortener.repository.UserRepository;
import com.url.shortener.security.jwt.JwtAuthenticationResponse;
import com.url.shortener.security.jwt.JwtUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// Handles user registration, login, and username lookup
@Service
@AllArgsConstructor
public class UserService {

    private PasswordEncoder passwordEncoder;    // BCrypt — configured in WebSecurityConfig
    private UserRepository userRepository;
    private AuthenticationManager authenticationManager; // triggers the full Spring Security auth flow
    private JwtUtils jwtUtils;

    // Hashes password before saving — never store plain text
    // Called by AuthController.registerUser()
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // Full login flow: verify credentials → set SecurityContext → generate JWT
    public JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest) {

        // Internally calls UserDetailsServiceImpl.loadUserByUsername() + bcrypt comparison
        // Throws AuthenticationException if credentials are wrong
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // Store in SecurityContext — makes the user available as Principal in this thread
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Cast is safe — principal is always UserDetailsImpl after our auth flow
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Generate signed JWT — returned to client, sent in Authorization header on future requests
        String jwt = jwtUtils.generateToken(userDetails);
        return new JwtAuthenticationResponse(jwt);
    }

    // Resolves username → full User entity
    // Used in controllers to convert Principal (just a name) into a User object for DB operations
    public User findByUsername(String name) {
        return userRepository.findByUsername(name)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username : " + name));
    }
}
