package com.eventhub.repository;

import com.eventhub.model.entity.Review;
import com.eventhub.model.entity.User;
import com.eventhub.model.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByEvent(Event event);
    
    Page<Review> findByEvent(Event event, Pageable pageable);
    
    List<Review> findByUser(User user);
    
    List<Review> findByEventOrderByCreatedAtDesc(Event event);
    
    @Query("SELECT r FROM Review r WHERE r.event.id = :eventId ORDER BY r.createdAt DESC")
    List<Review> findByEventIdOrderByCreatedAtDesc(@Param("eventId") Long eventId);
    
    @Query("SELECT r FROM Review r WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<Review> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.event.id = :eventId")
    Double getAverageRatingByEventId(@Param("eventId") Long eventId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.event.id = :eventId")
    Long getReviewCountByEventId(@Param("eventId") Long eventId);
    
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.event.id = :eventId GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistributionByEventId(@Param("eventId") Long eventId);
    
    Optional<Review> findByUserAndEvent(User user, Event event);
    
    boolean existsByUserAndEvent(User user, Event event);
    
    @Query("SELECT r FROM Review r WHERE r.rating >= :minRating")
    List<Review> findByRatingGreaterThanEqual(@Param("minRating") Integer minRating);
    
    @Query("SELECT r FROM Review r WHERE r.rating <= :maxRating")
    List<Review> findByRatingLessThanEqual(@Param("maxRating") Integer maxRating);
    
    @Query("SELECT COUNT(r) FROM Review r")
    Long getTotalReviewsCount();
    
    @Query("SELECT AVG(r.rating) FROM Review r")
    Double getOverallAverageRating();
}