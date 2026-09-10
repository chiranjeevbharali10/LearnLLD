package com.lldpractice.controller;

import com.lldpractice.domain.Attempt;
import com.lldpractice.dto.AttemptResponse;
import com.lldpractice.dto.SubmissionRequest;
import com.lldpractice.service.AttemptService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class AttemptController {

    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @PostMapping("/problems/{problemId}/attempts")
    public AttemptResponse createAttempt(@PathVariable Long problemId, @Valid @RequestBody SubmissionRequest request) {
        Attempt attempt = attemptService.createAttempt(problemId, request);
        return AttemptResponse.fromEntity(attempt);
    }

    @GetMapping("/attempts/{id}")
    public AttemptResponse getAttempt(@PathVariable Long id) {
        Attempt attempt = attemptService.getAttempt(id);
        return AttemptResponse.fromEntity(attempt);
    }

    @GetMapping("/attempts")
    public List<AttemptResponse> getAttempts(@RequestParam(required = false) Long problemId) {
        return attemptService.getAttempts(problemId).stream()
                .map(AttemptResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
