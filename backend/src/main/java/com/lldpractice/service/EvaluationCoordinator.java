package com.lldpractice.service;

import com.lldpractice.domain.Attempt;
import com.lldpractice.domain.AttemptStatus;
import com.lldpractice.domain.Evaluation;
import com.lldpractice.repository.AttemptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EvaluationCoordinator {

    private static final Logger log = LoggerFactory.getLogger(EvaluationCoordinator.class);

    private final AttemptRepository attemptRepository;
    private final Evaluator evaluator;

    public EvaluationCoordinator(AttemptRepository attemptRepository, Evaluator evaluator) {
        this.attemptRepository = attemptRepository;
        this.evaluator = evaluator;
    }

    @Async
    public void startEvaluation(Long attemptId) {
        log.info("Starting asynchronous evaluation for attempt ID: {}", attemptId);

        Attempt attempt = null;
        for (int i = 0; i < 5; i++) {
            attempt = attemptRepository.findById(attemptId).orElse(null);
            if (attempt != null) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        if (attempt == null) {
            log.error("Attempt ID {} not found in database after retries. Aborting evaluation.", attemptId);
            return;
        }

        if (attempt.getStatus() != AttemptStatus.SUBMITTED) {
            log.warn("Attempt ID {} is not in SUBMITTED state (current: {}). Skipping.", attemptId, attempt.getStatus());
            return;
        }

        attempt.setStatus(AttemptStatus.EVALUATING);
        attemptRepository.saveAndFlush(attempt);
        log.info("Attempt ID {} marked as EVALUATING", attemptId);

        try {
            String statement = (attempt.getProblem() != null && attempt.getProblem().getStatement() != null)
                    ? attempt.getProblem().getStatement() : "";
            String requirements = (attempt.getProblem() != null && attempt.getProblem().getRequirements() != null)
                    ? attempt.getProblem().getRequirements() : "";
            String assumptions = (attempt.getProblem() != null && attempt.getProblem().getAssumptions() != null)
                    ? attempt.getProblem().getAssumptions() : "";

            String fullRequirements = statement + "\n" + requirements + "\n" + assumptions;

            Evaluation evaluation = evaluator.evaluate(attempt.getSubmission(), fullRequirements);
            
            attempt.setEvaluation(evaluation);
            attempt.setStatus(AttemptStatus.COMPLETED);
            attemptRepository.save(attempt);
            log.info("Attempt ID {} successfully evaluated and marked COMPLETED", attemptId);
        } catch (Exception e) {
            log.error("Evaluation failed for attempt ID {}: {}", attemptId, e.getMessage(), e);
            attempt.setStatus(AttemptStatus.FAILED);
            attempt.setEvaluation(null);
            try {
                attemptRepository.save(attempt);
            } catch (Exception ex) {
                log.error("Failed to save FAILED status for attempt ID {}: {}", attemptId, ex.getMessage());
            }
        }
    }
}
