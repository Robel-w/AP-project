package com.civicpulse.backend.repository;

import com.civicpulse.backend.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByUserId(Long userId);
    List<Feedback> findByCategory(String category);

    List<Feedback> findByLatitudeBetweenAndLongitudeBetween(Double minLat, Double maxLat, Double minLng, Double maxLng);
    List<Feedback> findByCategoryAndLatitudeBetweenAndLongitudeBetween(String category, Double minLat, Double maxLat, Double minLng, Double maxLng);
}
