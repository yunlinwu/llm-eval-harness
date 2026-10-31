package com.yunlinwu.llmevalharness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link EvalRunnerService} with its collaborators mocked.
 *
 * Contrast with EvalControllerTest: that one boots the whole Spring app
 * (@SpringBootTest). This one boots NOTHING. We build the service ourselves and
 * hand it fakes, so we can script exactly what the database and "Claude" do.
 *
 * The three moves of every Mockito test:
 *   1. Arrange  — when(mock.method(...)).thenReturn(...)   script the fakes
 *   2. Act      — call the real method under test
 *   3. Assert   — check the result, and verify(mock) the interactions
 *
 * Note ObjectMapper is NOT mocked: it's fast, deterministic, and parsing JSON is
 * part of the behaviour we want to exercise. Mock what's slow, external, or
 * non-deterministic; use the real thing for everything else.
 */
@ExtendWith(MockitoExtension.class)
class EvalRunnerServiceTest {

    private static final String SYSTEM_PROMPT = "test system prompt";

    @Mock
    private EvalRepository evalRepository;

    @Mock
    private EvalRunRepository evalRunRepository;

    @Mock
    private ClaudeService claudeService;

    private EvalRunnerService service;

    @BeforeEach
    void setUp() {
        // Constructor injection is what makes this possible: no Spring, just `new`.
        service = new EvalRunnerService(
                evalRepository, evalRunRepository, claudeService,
                new ObjectMapper(), SYSTEM_PROMPT);
    }

    /** Helper: an Eval with id 1 and the given test cases as JSON. */
    private Eval evalWithCases(String testCasesJson) {
        Eval eval = new Eval();
        eval.setId(1L);
        eval.setName("arithmetic");
        eval.setTestCasesJson(testCasesJson);
        return eval;
    }

    @Test
    void missingEvalReturns404AndNeverCallsClaude() {
        // Arrange: the database has no eval 99.
        when(evalRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert: running it throws a 404.
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.run(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

        // The interesting assertion: we must not spend money calling Claude, and
        // must not record a run, for an eval that doesn't exist.
        verifyNoInteractions(claudeService, evalRunRepository);
    }

    @Test
    void scoresEachCaseAndSavesTheRun() {
        Eval eval = evalWithCases("""
                [{"input":"What is 2+2?","expectedOutput":"4"},
                 {"input":"Capital of France?","expectedOutput":"Paris"}]
                """);

        // ---- ARRANGE: give each stand-in its script ----
        // The fake database hands back our eval.
        when(evalRepository.findById(1L)).thenReturn(Optional.of(eval));
        // Fake-Claude gets the first question right and the second wrong ("Bob" doesn't contain "Paris").
        when(claudeService.complete(eq(SYSTEM_PROMPT), eq("What is 2+2?"))).thenReturn("The answer is 4.");
        when(claudeService.complete(eq(SYSTEM_PROMPT), eq("Capital of France?"))).thenReturn("Bob");
        // A fake repository doesn't really save anything, so hand back the run the service built.
        when(evalRunRepository.save(any(EvalRun.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ---- ACT: run the real code ----
        EvalRun result = service.run(1L);

        // ---- ASSERT: 2 cases, 1 passed, linked to eval #1 ----
        assertEquals(2, result.getTotalCount());
        assertEquals(1, result.getPassedCount());
        assertEquals(1L, result.getEvalId());
    }
}
