package com.url.shortener.repository;
import com.url.shortener.models.ClickEvent;
import com.url.shortener.models.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

// Repository for ClickEvent — Spring Data JPA auto-implements all query methods at runtime
@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    // Fetch clicks for a single URL within a datetime range
    // Used by: GET /api/urls/analytics/{shortUrl}
    // SQL equivalent: WHERE url_mapping_id = ? AND click_date BETWEEN ? AND ?
    List<ClickEvent> findByUrlMappingAndClickDateBetween(UrlMapping mapping,
                                                         LocalDateTime startDate,
                                                         LocalDateTime endDate);

    // Fetch clicks across multiple URLs within a datetime range
    // Used by: GET /api/urls/totalClicks (aggregates all URLs owned by a user)
    // SQL equivalent: WHERE url_mapping_id IN (...) AND click_date BETWEEN ? AND ?
    List<ClickEvent> findByUrlMappingInAndClickDateBetween(List<UrlMapping> urlMappings,
                                                           LocalDateTime startDate,
                                                           LocalDateTime endDate);
}
