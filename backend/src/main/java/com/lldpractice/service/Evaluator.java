package com.lldpractice.service;

import com.lldpractice.domain.Evaluation;
import com.lldpractice.domain.Submission;

public interface Evaluator {
    Evaluation evaluate(Submission submission, String problemRequirements);
}
