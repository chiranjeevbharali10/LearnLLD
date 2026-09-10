package com.lldpractice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmissionRequest {
    @NotBlank(message = "Format is required")
    private String format;
    
    @NotBlank(message = "Content cannot be empty")
    private String content;
}
