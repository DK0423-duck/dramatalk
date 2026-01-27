package com.dramatalk.domain.rating;

import com.dramatalk.domain.drama.Drama;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Rating {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Drama drama;

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal score; // 1.0 ~ 5.0 (0.5 단위)

    @Column(length = 300)
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Rating() {}

    public Rating(Drama drama, BigDecimal score, String comment) {
        this.drama = drama;
        this.score = score;
        this.comment = comment;
    }

    // getters
    public Long getId() { return id; }
    public Drama getDrama() { return drama; }
    public BigDecimal getScore() { return score; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
