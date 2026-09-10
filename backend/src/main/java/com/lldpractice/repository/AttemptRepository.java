package com.lldpractice.repository;

import com.lldpractice.domain.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    List<Attempt> findByProblemIdOrderByIdDesc(Long problemId);
    List<Attempt> findAllByOrderByIdDesc();
}
