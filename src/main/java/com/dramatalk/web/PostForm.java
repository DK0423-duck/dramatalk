package com.dramatalk.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PostForm {
    @NotBlank @Size(max = 120)
    private String title;

    @NotBlank @Size(max = 4000)
    private String content;

    public String getTitle() { return title; }
    public String getContent() { return content; }

    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
}
