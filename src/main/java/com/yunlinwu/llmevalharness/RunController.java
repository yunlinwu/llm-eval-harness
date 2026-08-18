package com.yunlinwu.llmevalharness;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP endpoints for RUNNING evals and reading results. Kept separate from
 * {@code EvalController} so each controller owns one resource: EvalController is
 * the eval definitions, RunController is the runs. Organizing controllers by
 * resource is the conventional Spring layout once you have more than one.
 */
@RestController
public class RunController {

    private final EvalRunnerService evalRunnerService;
    private final EvalRunRepository evalRunRepository;

    public RunController(EvalRunnerService evalRunnerService, EvalRunRepository evalRunRepository) {
        this.evalRunnerService = evalRunnerService;
        this.evalRunRepository = evalRunRepository;
    }

    /**
     * Run eval {id} against the model. This actually calls Claude once per test
     * case, so it needs ANTHROPIC_API_KEY set and spends a few tokens.
     *
     *   curl -X POST http://localhost:8080/evals/1/runs
     */
    @PostMapping("/evals/{id}/runs")
    public ResponseEntity<EvalRun> run(@PathVariable Long id) {
        return ResponseEntity.status(201).body(evalRunnerService.run(id));
    }

    /** Fetch a past run's full results (including the per-case JSON). */
    @GetMapping("/runs/{id}")
    public ResponseEntity<EvalRun> get(@PathVariable Long id) {
        return evalRunRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
