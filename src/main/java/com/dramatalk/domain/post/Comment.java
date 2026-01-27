package com.dramatalk.domain.post;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Comment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Post post;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Comment() {}

    public Comment(Post post, String content) {
        this.post = post;
        this.content = content;
    }

    public Long getId() { return id; }
    public Post getPost() { return post; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
