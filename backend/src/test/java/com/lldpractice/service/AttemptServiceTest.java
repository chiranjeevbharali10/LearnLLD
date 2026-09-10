package com.lldpractice.service;

import com.lldpractice.domain.Attempt;
import com.lldpractice.domain.AttemptStatus;
import com.lldpractice.domain.Problem;
import com.lldpractice.dto.SubmissionRequest;
import com.lldpractice.repository.AttemptRepository;
import com.lldpractice.repository.ProblemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AttemptServiceTest {

    private AttemptRepository attemptRepository;
    private ProblemRepository problemRepository;
    private EvaluationCoordinator evaluationCoordinator;
    private AttemptService attemptService;

    @BeforeEach
    void setUp() {
        attemptRepository = mock(AttemptRepository.class);
        problemRepository = mock(ProblemRepository.class);
        evaluationCoordinator = mock(EvaluationCoordinator.class);
        attemptService = new AttemptService(attemptRepository, problemRepository, evaluationCoordinator);
    }

    @Test
    void testDeterministicValidation_ShortContent() {
        SubmissionRequest request = new SubmissionRequest();
        request.setFormat("TEXT");
        request.setContent("Too short");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            attemptService.createAttempt(1L, request);
        });
        
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("too short or empty"));
    }

    @Test
    void testDuplicateSubmissionGuard() {
        Problem problem = new Problem();
        problem.setId(1L);

        Attempt existingAttempt = new Attempt();
        existingAttempt.setStatus(AttemptStatus.EVALUATING);
        
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(attemptRepository.findByProblemIdOrderByIdDesc(1L)).thenReturn(List.of(existingAttempt));

        SubmissionRequest request = new SubmissionRequest();
        request.setFormat("TEXT");
        request.setContent("This is a valid length content for the submission.");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            attemptService.createAttempt(1L, request);
        });

        assertEquals(409, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("already being evaluated"));
    }

    @Test
    void testStateMachineTransition_Submitted() {
        Problem problem = new Problem();
        problem.setId(1L);

        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(attemptRepository.findByProblemIdOrderByIdDesc(1L)).thenReturn(List.of());
        when(attemptRepository.save(any(Attempt.class))).thenAnswer(i -> {
            Attempt a = i.getArgument(0);
            a.setId(100L);
            return a;
        });

        SubmissionRequest request = new SubmissionRequest();
        request.setFormat("TEXT");
        request.setContent("This is a valid length content for the submission which meets criteria.");

        Attempt attempt = attemptService.createAttempt(1L, request);

        assertEquals(AttemptStatus.SUBMITTED, attempt.getStatus());
        verify(evaluationCoordinator, times(1)).startEvaluation(100L);
    }
}
