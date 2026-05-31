package com.url.shortener.models;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

// Entity: maps a short code to an original URL, owned by a user
// Tracks click count and links to individual click events for analytics
@Entity
@Data
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment PK
    private Long id;

    private String originalUrl;  // the full URL user wants to shorten
    private String shortUrl;     // generated short code (e.g. "abc123")
    private int clickCount = 0;  // running total, incremented on every redirect
    private LocalDateTime createdDate;

    // Many URLs can belong to one user
    // DB column: user_id (FK → user.id)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // One URL can have many click events (for date-wise analytics)
    // mappedBy = "urlMapping" → ClickEvent owns the FK, this side is read-only
    @OneToMany(mappedBy = "urlMapping")
    private List<ClickEvent> clickEvents;
}
