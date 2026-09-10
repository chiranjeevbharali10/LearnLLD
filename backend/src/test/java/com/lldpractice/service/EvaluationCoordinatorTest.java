package com.lldpractice.service;

import com.lldpractice.domain.Attempt;
import com.lldpractice.domain.AttemptStatus;
import com.lldpractice.domain.Evaluation;
import com.lldpractice.domain.Feedback;
import com.lldpractice.domain.Problem;
import com.lldpractice.domain.Submission;
import com.lldpractice.repository.AttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class EvaluationCoordinatorTest {

    private AttemptRepository attemptRepository;
    private Evaluator evaluator;
    private EvaluationCoordinator coordinator;

    @BeforeEach
    void setUp() {
        attemptRepository = mock(AttemptRepository.class);
        evaluator = mock(Evaluator.class);
        coordinator = new EvaluationCoordinator(attemptRepository, evaluator);
    }

    @Test
    void testEvaluationSuccess_MockedEvaluator() {
        Attempt attempt = new Attempt();
        attempt.setId(1L);
        attempt.setStatus(AttemptStatus.SUBMITTED);
        
        Problem p = new Problem();
        p.setStatement("Stmt");
        p.setRequirements("Req");
        p.setAssumptions("Assum");
        attempt.setProblem(p);
        
        Submission s = new Submission();
        s.setContent("Design");
        attempt.setSubmission(s);

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        
        Evaluation mockEval = new Evaluation();
        mockEval.setCriteria(List.of(new Feedback("req", 10, "evid", null, "sugg", "high")));
        when(evaluator.evaluate(any(Submission.class), anyString())).thenReturn(mockEval);

        coordinator.startEvaluation(1L);

        ArgumentCaptor<Attempt> captor = ArgumentCaptor.forClass(Attempt.class);
        // saveAndFlush is called for EVALUATING, save is called for COMPLETED
        verify(attemptRepository, times(1)).save(captor.capture());
        
        Attempt saved = captor.getValue();
        assertEquals(AttemptStatus.COMPLETED, saved.getStatus());
        assertNotNull(saved.getEvaluation());
        assertEquals(10, saved.getEvaluation().getCriteria().get(0).getScore());
    }

    @Test
    void testExplicitFailurePath_LlmFails() {
        Attempt attempt = new Attempt();
        attempt.setId(1L);
        attempt.setStatus(AttemptStatus.SUBMITTED);
        
        Problem p = new Problem();
        p.setStatement("Stmt");
        p.setRequirements("Req");
        p.setAssumptions("Assum");
        attempt.setProblem(p);
        
        attempt.setSubmission(new Submission());

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        
        when(evaluator.evaluate(any(Submission.class), anyString())).thenThrow(new RuntimeException("API Timeout"));

        coordinator.startEvaluation(1L);

        ArgumentCaptor<Attempt> captor = ArgumentCaptor.forClass(Attempt.class);
        verify(attemptRepository, times(1)).save(captor.capture());
        
        Attempt saved = captor.getValue();
        // The attempt ends in FAILED, not stuck in EVALUATING
        assertEquals(AttemptStatus.FAILED, saved.getStatus());
    }
}
