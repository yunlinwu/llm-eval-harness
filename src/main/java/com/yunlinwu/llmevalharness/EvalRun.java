package com.yunlinwu.llmevalharness;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

/**
 * An EvalRun is the RESULT of running an {@link Eval} once against a model.
 *
 * Second {@code @Entity} — same pattern as {@code Eval}, so this should feel
 * familiar now. Note {@code evalId} is a plain {@code Long}, not a JPA
 * {@code @ManyToOne} relationship to {@code Eval}. That keeps things simple (no
 * entity graph to reason about); the tradeoff is the database won't enforce the
 * foreign key for us. Good enough for a learning project — you'd reach for
 * {@code @ManyToOne} once you want to navigate {@code run.getEval()} in code.
 */
@Entity
public class EvalRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long evalId;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private int totalCount;

    @Column(nullable = false)
    private int passedCount;

    // Per-test-case results as a JSON string — same embedded-JSON trick as
    // Eval.testCasesJson. Shape: [{"input","expected","actual","passed"}, ...]
    @Lob
    @Column(name = "results_json", columnDefinition = "TEXT")
    private String resultsJson;

    @Column(nullable = false)
    private Instant createdAt;

    public EvalRun() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEvalId() { return evalId; }
    public void setEvalId(Long evalId) { this.evalId = evalId; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

    public int getPassedCount() { return passedCount; }
    public void setPassedCount(int passedCount) { this.passedCount = passedCount; }

    public String getResultsJson() { return resultsJson; }
    public void setResultsJson(String resultsJson) { this.resultsJson = resultsJson; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
