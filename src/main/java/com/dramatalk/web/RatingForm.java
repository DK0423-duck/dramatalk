package com.dramatalk.web;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class RatingForm {

    @NotNull
    @DecimalMin("1.0")
    @DecimalMax("5.0")
    private BigDecimal score;

    @Size(max = 300)
    private String comment;

    @AssertTrue(message = "점수는 0.5 단위(예: 1.0, 1.5, 2.0 ...)여야 합니다.")
    public boolean isHalfStep() {
        if (score == null) return true;
        // score * 2 가 정수면 0.5 단위
        BigDecimal twice = score.multiply(BigDecimal.valueOf(2));
        return twice.stripTrailingZeros().scale() <= 0;
    }

    public BigDecimal getScore() { return score; }
    public String getComment() { return comment; }

    public void setScore(BigDecimal score) { this.score = score; }
    public void setComment(String comment) { this.comment = comment; }
}
