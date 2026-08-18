package com.yunlinwu.llmevalharness;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * THE thing an eval harness does: take a stored {@link Eval}, run every test case
 * against the model, score each answer, and record the results.
 *
 * This is why the app finally earns a Service layer (Evening 1 didn't have one):
 * there's real multi-step logic here — load, parse, call the LLM N times, score,
 * summarize, save — that doesn't belong in a controller and isn't a single DB op.
 * That "the logic outgrew the controller" moment is exactly when you add a
 * {@code @Service}.
 *
 * Constructor injection: Spring hands us the two repositories, the Claude client,
 * and its own pre-configured Jackson {@link ObjectMapper}. We create none of them.
 */
@Service
public class EvalRunnerService {

    private final EvalRepository evalRepository;
    private final EvalRunRepository evalRunRepository;
    private final ClaudeService claudeService;
    private final ObjectMapper objectMapper;
    private final String systemPrompt;

    public EvalRunnerService(EvalRepository evalRepository,
                             EvalRunRepository evalRunRepository,
                             ClaudeService claudeService,
                             ObjectMapper objectMapper,
                             @Value("${eval.system-prompt}") String systemPrompt) {
        this.evalRepository = evalRepository;
        this.evalRunRepository = evalRunRepository;
        this.claudeService = claudeService;
        this.objectMapper = objectMapper;
        this.systemPrompt = systemPrompt;
    }

    public EvalRun run(Long evalId) {
        // 1. Load the eval (404 if it doesn't exist).
        Eval eval = evalRepository.findById(evalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "eval " + evalId + " not found"));

        // 2. Parse its test cases out of the stored JSON string.
        List<TestCase> testCases = parseTestCases(eval.getTestCasesJson());

        // 3. For each case: ask Claude, then score the answer.
        List<CaseResult> results = new ArrayList<>();
        int passed = 0;
        for (TestCase testCase : testCases) {
            String actual = claudeService.complete(systemPrompt, testCase.input());
            boolean ok = Scorer.passes(testCase.expectedOutput(), actual);
            if (ok) {
                passed++;
            }
            results.add(new CaseResult(testCase.input(), testCase.expectedOutput(), actual, ok));
        }

        // 4. Record the run.
        EvalRun evalRun = new EvalRun();
        evalRun.setEvalId(eval.getId());
        evalRun.setModel(claudeService.getModel());
        evalRun.setTotalCount(testCases.size());
        evalRun.setPassedCount(passed);
        evalRun.setResultsJson(writeJson(results));
        evalRun.setCreatedAt(Instant.now());
        return evalRunRepository.save(evalRun);
    }

    private List<TestCase> parseTestCases(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<TestCase>>() {});
        } catch (Exception e) {
            // Remember Evening 1's experiment #5? The harness happily STORED
            // "not even valid json lol". Here is where that finally bites — so we
            // turn it into a clean 400 instead of a 500.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "eval's testCasesJson is not a valid list of {input, expectedOutput}");
        }
    }

    private String writeJson(List<CaseResult> results) {
        try {
            return objectMapper.writeValueAsString(results);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "could not serialize results");
        }
    }
}
