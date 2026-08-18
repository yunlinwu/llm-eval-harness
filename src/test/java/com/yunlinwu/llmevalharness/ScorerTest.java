package com.yunlinwu.llmevalharness;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Pure unit tests for the scorer — no Spring context, no API calls, no API key.
 * This is the payoff of keeping scoring a pure static function: the logic that
 * decides pass/fail is testable in isolation and runs in microseconds. The
 * expensive, non-deterministic part (calling Claude) is NOT tested here on
 * purpose — you don't want your test suite spending tokens or depending on a
 * network. Test the deterministic logic; leave the LLM call for manual runs.
 */
class ScorerTest {

    @Test
    void exactMatchPasses() {
        assertTrue(Scorer.passes("4", "4"));
    }

    @Test
    void containsMatchPasses() {
        // The whole reason we use "contains": real LLM answers aren't byte-identical.
        assertTrue(Scorer.passes("4", "The answer is 4."));
        assertTrue(Scorer.passes("Paris", "The capital of France is Paris."));
    }

    @Test
    void caseAndWhitespaceInsensitive() {
        assertTrue(Scorer.passes("  PARIS  ", "paris"));
    }

    @Test
    void wrongAnswerFails() {
        assertFalse(Scorer.passes("4", "5"));
        assertFalse(Scorer.passes("Paris", "London"));
    }

    @Test
    void nullsFail() {
        assertFalse(Scorer.passes(null, "4"));
        assertFalse(Scorer.passes("4", null));
    }

    @Test
    void emptyExpectedFails() {
        // An empty expected string would "contain-match" everything — guard against it.
        assertFalse(Scorer.passes("", "anything"));
    }
}
