package com.url.shortener.controller;
import com.url.shortener.models.UrlMapping;
import com.url.shortener.service.UrlMappingService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

// Handles the core redirect logic: short URL → original URL
// No /api prefix — lives at root so short links look clean (e.g. localhost:8080/abc123)
@RestController
@AllArgsConstructor
public class RedirectController {

    private UrlMappingService urlMappingService;

    // GET /{shortUrl}
    // Looks up the short code, redirects to original URL if found
    // Returns 302 (temporary redirect) — browser follows the Location header automatically
    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl) {

        UrlMapping urlMapping = urlMappingService.getOriginalUrl(shortUrl);

        if (urlMapping != null) {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Location", urlMapping.getOriginalUrl()); // tells browser where to go
            return ResponseEntity.status(302).headers(httpHeaders).build(); // 302 = Found (redirect)
        } else {
            return ResponseEntity.notFound().build(); // 404 if short code doesn't exist in DB
        }
    }
}
