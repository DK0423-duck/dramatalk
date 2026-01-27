package com.dramatalk.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DramaForm {

    private Long id; // 수정 시 사용

    @NotBlank
    @Size(max = 120)
    private String title;

    @Size(max = 60)
    private String year;

    @Size(max = 80)
    private String genre;

    @Size(max = 1000)
    private String synopsis;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getYear() { return year; }
    public String getGenre() { return genre; }
    public String getSynopsis() { return synopsis; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setYear(String year) { this.year = year; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setSynopsis(String synopsis) { this.synopsis = synopsis; }
}
