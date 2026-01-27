package com.dramatalk.domain.post;

import com.dramatalk.domain.drama.Drama;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Post {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Drama drama;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Post() {}

    public Post(Drama drama, String title, String content) {
        this.drama = drama;
        this.title = title;
        this.content = content;
    }

    public Long getId() { return id; }
    public Drama getDrama() { return drama; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
