package com.url.shortener.repository;
import com.url.shortener.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// Repository for User — handles all DB operations for user records
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Fetch a user by their username
    // Used by: login, JWT validation, and resolving Principal in controllers
    // SQL equivalent: WHERE username = ?
    // Optional forces null-safe handling at the call site — no accidental NPEs
    Optional<User> findByUsername(String username);
}
