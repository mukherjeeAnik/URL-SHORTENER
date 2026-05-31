package com.url.shortener.repository;
import com.url.shortener.models.UrlMapping;
import com.url.shortener.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// Repository for UrlMapping — handles all DB operations for short URL records
@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    // Fetch a single URL mapping by its short code
    // Used by: redirect flow and analytics lookup
    // SQL equivalent: WHERE short_url = ?
    // Returns null if not found — consider Optional<UrlMapping> for safer null handling
    UrlMapping findByShortUrl(String shortUrl);

    // Fetch all URLs created by a specific user
    // Used by: GET /api/urls/myurls
    // SQL equivalent: WHERE user_id = ?
    List<UrlMapping> findByUser(User user);
}
