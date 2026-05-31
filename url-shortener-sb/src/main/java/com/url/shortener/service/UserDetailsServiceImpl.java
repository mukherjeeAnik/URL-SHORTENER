package com.url.shortener.service;
import com.url.shortener.models.User;
import com.url.shortener.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Implements Spring Security's UserDetailsService — the required hook for authentication
// Spring Security calls loadUserByUsername() automatically during login
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository; // consider constructor injection over @Autowired field injection

    // Fetches user from DB by username and converts to UserDetails for Spring Security
    // @Transactional: keeps DB session open during the build() call (needed if any lazy fields are accessed)
    // Throws UsernameNotFoundException → Spring Security maps this to a 401 response automatically
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username : " + username));
        return UserDetailsImpl.build(user); // converts User entity → UserDetailsImpl
    }
}
