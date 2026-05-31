package com.url.shortener.models;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

// Entity: represents a single click event on a short URL
// Each row = one click, linked to which URL was clicked and when
@Entity
@Data // Lombok: generates getters, setters, equals, hashCode, toString
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment PK
    private Long id;

    private LocalDateTime clickDate; // timestamp of when the click happened

    // Many clicks can belong to one URL mapping
    // DB column: url_mapping_id (FK → url_mapping.id)
    @ManyToOne
    @JoinColumn(name = "url_mapping_id")
    private UrlMapping urlMapping;
}
