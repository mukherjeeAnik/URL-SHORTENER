package com.url.shortener.service;
import com.url.shortener.dtos.ClickEventDTO;
import com.url.shortener.dtos.UrlMappingDTO;
import com.url.shortener.models.ClickEvent;
import com.url.shortener.models.UrlMapping;
import com.url.shortener.models.User;
import com.url.shortener.repository.ClickEventRepository;
import com.url.shortener.repository.UrlMappingRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Core business logic for URL shortening, redirecting, and click analytics
@Service
@AllArgsConstructor
public class UrlMappingService {

    private UrlMappingRepository urlMappingRepository;
    private ClickEventRepository clickEventRepository;

    // Creates a short URL record and persists it to DB
    // @Transactional: rolls back if save fails midway
    @Transactional
    public UrlMappingDTO createShortUrl(String originalUrl, User user) {
        String shortUrl = generateShortUrl();
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setShortUrl(shortUrl);
        urlMapping.setUser(user);
        urlMapping.setCreatedDate(LocalDateTime.now());
        UrlMapping savedUrlMapping = urlMappingRepository.save(urlMapping);
        return convertToDto(savedUrlMapping);
    }

    // Maps UrlMapping entity → UrlMappingDTO (safe to expose via API, no sensitive fields)
    private UrlMappingDTO convertToDto(UrlMapping urlMapping) {
        UrlMappingDTO dto = new UrlMappingDTO();
        dto.setId(urlMapping.getId());
        dto.setOriginalUrl(urlMapping.getOriginalUrl());
        dto.setShortUrl(urlMapping.getShortUrl());
        dto.setClickCount(urlMapping.getClickCount());
        dto.setCreatedDate(urlMapping.getCreatedDate());
        dto.setUsername(urlMapping.getUser().getUsername());
        return dto;
    }

    // Generates a random 8-character alphanumeric short code (e.g. "aB3xQ7mZ")
    // Note: no collision check — two users could theoretically get the same code
    private String generateShortUrl() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();
        java.util.Random rnd = new java.util.Random();
        while (result.length() < 8) {
            result.append(characters.charAt(rnd.nextInt(characters.length())));
        }
        return result.toString();
    }

    // Fetches all URLs owned by a user and converts them to DTOs
    public List<UrlMappingDTO> getUrlsByUser(User user) {
        return urlMappingRepository.findByUser(user).stream()
                .map(this::convertToDto)
                .toList();
    }

    // Returns daily click counts for a specific short URL within a datetime range
    // Groups raw ClickEvent records by date → counts per day → maps to ClickEventDTO
    public List<ClickEventDTO> getClickEventsByDate(String shortUrl, LocalDateTime start, LocalDateTime end) {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (urlMapping != null) {
            return clickEventRepository
                    .findByUrlMappingAndClickDateBetween(urlMapping, start, end)
                    .stream()
                    .collect(Collectors.groupingBy(
                            click -> click.getClickDate().toLocalDate(), // group by date (drop time)
                            Collectors.counting()                         // count clicks per day
                    ))
                    .entrySet().stream()
                    .map(entry -> {
                        ClickEventDTO dto = new ClickEventDTO();
                        dto.setClickDate(entry.getKey());   // date
                        dto.setCount(entry.getValue());     // click count for that day
                        return dto;
                    })
                    .collect(Collectors.toList());
        }
        return null; // returns null if short URL not found — consider returning empty list instead
    }

    // Returns total clicks per day across ALL URLs of a user within a date range
    // end.plusDays(1).atStartOfDay() makes the range inclusive of the end date
    public Map<LocalDate, Long> getTotalClicksByUserAndDate(User user, LocalDate start, LocalDate end) {
        List<UrlMapping> urlMappings = urlMappingRepository.findByUser(user);
        List<ClickEvent> clickEvents = clickEventRepository
                .findByUrlMappingInAndClickDateBetween(
                        urlMappings,
                        start.atStartOfDay(),              // 2024-01-01T00:00:00
                        end.plusDays(1).atStartOfDay()     // 2024-01-02T00:00:00 (exclusive upper bound trick)
                );
        // Group all click events by date and count — result: { 2024-01-01: 42, 2024-01-02: 17 }
        return clickEvents.stream()
                .collect(Collectors.groupingBy(
                        click -> click.getClickDate().toLocalDate(),
                        Collectors.counting()
                ));
    }

    // Handles redirect: increments click counter, records a ClickEvent, returns original URL
    // Two separate saves — consider @Transactional to keep both atomic
    public UrlMapping getOriginalUrl(String shortUrl) {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);

        if (urlMapping != null) {
            urlMapping.setClickCount(urlMapping.getClickCount() + 1); // increment running total
            urlMappingRepository.save(urlMapping);

            // Log individual click for date-wise analytics
            ClickEvent clickEvent = new ClickEvent();
            clickEvent.setClickDate(LocalDateTime.now());
            clickEvent.setUrlMapping(urlMapping);
            clickEventRepository.save(clickEvent);
        }

        return urlMapping; // null if short code not found — RedirectController handles the 404
    }
}
