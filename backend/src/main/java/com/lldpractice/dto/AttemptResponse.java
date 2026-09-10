package com.lldpractice.dto;

import com.lldpractice.domain.Attempt;
import com.lldpractice.domain.AttemptStatus;
import com.lldpractice.domain.Evaluation;
import lombok.Data;

@Data
public class AttemptResponse {
    private Long id;
    private Long problemId;
    private AttemptStatus status;
    private String format;
    private String content;
    private Evaluation evaluation;

    public static AttemptResponse fromEntity(Attempt attempt) {
        AttemptResponse res = new AttemptResponse();
        res.setId(attempt.getId());
        res.setProblemId(attempt.getProblem().getId());
        res.setStatus(attempt.getStatus());
        if (attempt.getSubmission() != null) {
            res.setFormat(attempt.getSubmission().getFormat());
            res.setContent(attempt.getSubmission().getContent());
        }
        res.setEvaluation(attempt.getEvaluation());
        return res;
    }
}
