package com.dramatalk.domain.drama;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Drama {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(name = "release_year", length = 60)
    private String year; // 예: "2003", "2023"

    @Column(length = 80)
    private String genre;

    @Column(length = 1000)
    private String synopsis;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Drama() {}

    public Drama(String title, String year, String genre, String synopsis) {
        this.title = title;
        this.year = year;
        this.genre = genre;
        this.synopsis = synopsis;
    }

    // getters/setters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getYear() { return year; }
    public String getGenre() { return genre; }
    public String getSynopsis() { return synopsis; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setTitle(String title) { this.title = title; }
    public void setYear(String year) { this.year = year; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setSynopsis(String synopsis) { this.synopsis = synopsis; }
}
