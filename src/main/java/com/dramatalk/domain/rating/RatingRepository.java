package com.dramatalk.domain.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByDramaIdOrderByCreatedAtDesc(Long dramaId);

    @Query("select avg(r.score) from Rating r where r.drama.id = :dramaId")
    Double findAverageScore(Long dramaId);

    @Transactional
    void deleteByDramaId(Long dramaId);
}

