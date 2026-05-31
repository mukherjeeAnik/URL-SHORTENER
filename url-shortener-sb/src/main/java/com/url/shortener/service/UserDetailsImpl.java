package com.url.shortener.service;
import com.url.shortener.models.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

// Adapts the app's User entity into Spring Security's UserDetails interface
// Spring Security works with UserDetails, not User directly — this is the bridge
@Data
@NoArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L; // required for Serializable (UserDetails extends it)

    private Long id;
    private String username;
    private String email;
    private String password; // bcrypt hash — never plain text
    private Collection<? extends GrantedAuthority> authorities; // roles e.g. [ROLE_USER]

    public UserDetailsImpl(Long id, String username, String email, String password,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    // Static factory: converts a User entity → UserDetailsImpl
    // Wraps the single role string (e.g. "ROLE_USER") into a GrantedAuthority collection
    // Called by UserDetailsServiceImpl.loadUserByUsername()
    public static UserDetailsImpl build(User user) {
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                Collections.singleton(authority) // singleton = only one role per user
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // NOTE: isAccountNonExpired, isAccountNonLocked, isCredentialsNonExpired, isEnabled
    // are not overridden — all default to true (UserDetails interface default methods)
    // Override these if you need account locking, expiry, or soft-delete support
}
