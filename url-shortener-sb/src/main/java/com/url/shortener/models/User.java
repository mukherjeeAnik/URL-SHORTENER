package com.url.shortener.models;
import jakarta.persistence.*;
import lombok.Data;

// Entity: represents a registered user
// Table name is "users" — "user" is a reserved keyword in most SQL databases
@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment PK
    private Long id;

    private String email;
    private String username;
    private String password; // stored as bcrypt hash, never plain text

    // Default role assigned at registration — used by Spring Security for @PreAuthorize checks
    private String role = "ROLE_USER"; // prefix "ROLE_" is required by Spring Security conventions
}
