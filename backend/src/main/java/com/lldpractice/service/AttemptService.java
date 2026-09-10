package com.lldpractice.service;

import com.lldpractice.domain.Attempt;
import com.lldpractice.domain.AttemptStatus;
import com.lldpractice.domain.Problem;
import com.lldpractice.domain.Submission;
import com.lldpractice.dto.SubmissionRequest;
import com.lldpractice.repository.AttemptRepository;
import com.lldpractice.repository.ProblemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AttemptService {

    private final AttemptRepository attemptRepository;
    private final ProblemRepository problemRepository;
    private final EvaluationCoordinator evaluationCoordinator;

    public AttemptService(AttemptRepository attemptRepository, ProblemRepository problemRepository, EvaluationCoordinator evaluationCoordinator) {
        this.attemptRepository = attemptRepository;
        this.problemRepository = problemRepository;
        this.evaluationCoordinator = evaluationCoordinator;
    }

    public Attempt createAttempt(Long problemId, SubmissionRequest request) {
        if (request.getContent() == null || request.getContent().trim().length() < 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Submission content is too short or empty.");
        }

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found"));

        // Guard against duplicate submission if already evaluating
        List<Attempt> existingAttempts = attemptRepository.findByProblemIdOrderByIdDesc(problemId);
        if (!existingAttempts.isEmpty()) {
            Attempt lastAttempt = existingAttempts.get(0);
            if (lastAttempt.getStatus() == AttemptStatus.SUBMITTED || lastAttempt.getStatus() == AttemptStatus.EVALUATING) {
                if (lastAttempt.getCreatedAt() != null && lastAttempt.getCreatedAt().isBefore(java.time.LocalDateTime.now().minusSeconds(60))) {
                    lastAttempt.setStatus(AttemptStatus.FAILED);
                    attemptRepository.save(lastAttempt);
                } else {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "An attempt is already being evaluated.");
                }
            }
        }

        Submission submission = Submission.builder()
                .format(request.getFormat())
                .content(request.getContent())
                .build();

        Attempt attempt = Attempt.builder()
                .problem(problem)
                .status(AttemptStatus.SUBMITTED)
                .submission(submission)
                .build();

        attempt = attemptRepository.save(attempt);

        // trigger evaluation asynchronously
        evaluationCoordinator.startEvaluation(attempt.getId());

        return attempt;
    }

    public Attempt getAttempt(Long id) {
        return attemptRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attempt not found"));
    }

    public List<Attempt> getAttempts(Long problemId) {
        if (problemId != null) {
            return attemptRepository.findByProblemIdOrderByIdDesc(problemId);
        }
        return attemptRepository.findAllByOrderByIdDesc();
    }
}
