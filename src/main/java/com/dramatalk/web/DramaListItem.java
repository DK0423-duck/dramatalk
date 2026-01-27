package com.dramatalk.web;

import java.math.BigDecimal;

public class DramaListItem {
    private final Long id;
    private final String title;
    private final String year;
    private final String genre;
    private final BigDecimal avgScore; // 없으면 null

    public DramaListItem(Long id, String title, String year, String genre, BigDecimal avgScore) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.genre = genre;
        this.avgScore = avgScore;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getYear() { return year; }
    public String getGenre() { return genre; }
    public BigDecimal getAvgScore() { return avgScore; }
}
