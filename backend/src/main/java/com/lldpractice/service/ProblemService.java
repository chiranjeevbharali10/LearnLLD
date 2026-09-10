package com.lldpractice.service;

import com.lldpractice.domain.Attempt;
import com.lldpractice.domain.AttemptStatus;
import com.lldpractice.domain.Problem;
import com.lldpractice.repository.AttemptRepository;
import com.lldpractice.repository.ProblemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final AttemptRepository attemptRepository;
    private final JdbcTemplate jdbcTemplate;

    public ProblemService(ProblemRepository problemRepository, AttemptRepository attemptRepository, JdbcTemplate jdbcTemplate) {
        this.problemRepository = problemRepository;
        this.attemptRepository = attemptRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE IF EXISTS evaluation_feedback ALTER COLUMN concern SET DATA TYPE TEXT");
                jdbcTemplate.execute("ALTER TABLE IF EXISTS evaluation_feedback ALTER COLUMN evidence SET DATA TYPE TEXT");
                jdbcTemplate.execute("ALTER TABLE IF EXISTS evaluation_feedback ALTER COLUMN suggestion SET DATA TYPE TEXT");
            } catch (Exception ignored) {}
            // Clean up any stale in-flight attempts from previous JVM runs
            List<Attempt> staleAttempts = attemptRepository.findAll().stream()
                    .filter(a -> a.getStatus() == AttemptStatus.SUBMITTED || a.getStatus() == AttemptStatus.EVALUATING)
                    .toList();
            for (Attempt stale : staleAttempts) {
                stale.setStatus(AttemptStatus.FAILED);
                attemptRepository.save(stale);
            }

            Problem p1 = problemRepository.findById(1L).orElse(new Problem());
            p1.setTitle("Design an Automated Parking Lot System");
            p1.setStatement("Design an automated, multi-level smart parking facility capable of managing real-time ingress and egress across several terminal gates concurrently. The facility needs to gracefully handle fluctuating peak demands, optimize slot allocation based on dynamic rules (e.g., closest to elevator vs. minimal floor traversal), and compute fine-grained fee tariffs upon departure.\n\nYour design must adhere to classical SOLID principles and illustrate high structural cohesion. Pay deliberate focus to decoupling parking spot allocation algorithms from core domain objects, ensuring robust thread synchronization around state-mutating transactions.");
            p1.setRequirements("Multi-Vehicle Tiers: Must support Motorcycle/Two-Wheeler, Compact Sedan, Large/SUV/Van, and Electric Vehicle (EV with charging dock). Pluggable Allocation Strategies: Support dynamic spot discovery policies. Automated Ticketing: Issue immutable entry tickets bearing unique UUID barcodes, assigned spot coordinates, vehicle license metadata, and nanosecond entry timestamps. Real-Time Occupancy Telemetry: Every parking floor and terminal display unit must publish live availability counts per vehicle classification. Tiered Tariff Engine: Dynamic payment calculation based on vehicle type multiplier, elapsed parking duration, and active EV surcharge units. Full Capacity Contingency: Reject entry tickets automatically if zero spots are available.");
            p1.setAssumptions("Thread Safety: Concurrent ingress at multiple gates must guarantee zero double-booking on spots without introducing coarse, global lock contention across the entire building. Extensibility: Adding an Autonomous Valet Drone terminal or contactless toll pass (FASTag/RFID) must require minimal modification to existing core entities. Boundary Bounds: Floors: ≤ 4. Total Spots/Floor: 500. Max Concurrency: 12 Gates. Ticket UUID: RFC 4122.");
            problemRepository.save(p1);

            Problem p2 = problemRepository.findById(2L).orElse(new Problem());
            p2.setTitle("Elevator System");
            p2.setStatement("Design a system for multiple elevators serving multiple floors. Must handle external hall requests (floor + up/down) and internal cabin requests (floor selection).");
            p2.setRequirements("Include a dispatch/scheduling strategy for assigning requests to elevators, and track each elevator's direction, current floor, and door state.");
            p2.setAssumptions("Assume constant time between floors. No weight limits.");
            problemRepository.save(p2);

            Problem p3 = problemRepository.findById(3L).orElse(new Problem());
            p3.setTitle("Vending Machine");
            p3.setStatement("Design a vending machine with states such as Idle, Selecting, Dispensing, and ReturningChange.");
            p3.setRequirements("Must accept payment (coins/notes), track per-slot inventory, dispense the selected item, handle insufficient payment or out-of-stock items, and return change correctly.");
            p3.setAssumptions("Assume a finite supply of change. Products have fixed sizes and prices.");
            problemRepository.save(p3);
        };
    }
}
