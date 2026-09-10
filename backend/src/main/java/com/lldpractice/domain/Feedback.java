package com.lldpractice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {
    private String criterion;
    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String evidence;

    @Column(columnDefinition = "TEXT")
    private String concern;

    @Column(columnDefinition = "TEXT")
    private String suggestion;

    private String confidence;
}
