package com.yunlinwu.llmevalharness;

/**
 * Decides whether the model's answer counts as correct.
 *
 * This is the heart of "evaluation" — and there is no single right way to do it.
 * We use a forgiving <b>contains</b> check: normalize both strings (trim +
 * lowercase) and pass if the model's answer contains the expected text. So an
 * expected value of "4" passes against "The answer is 4." — which is what you
 * want, because LLM output is rarely byte-identical to a canonical answer.
 *
 * It's a pure static function, so it's trivially unit-testable with no API calls
 * and no Spring context (see {@code ScorerTest}). Future scorers — exact match, or
 * an "LLM-as-judge" that asks Claude to grade the answer — would slot in beside
 * this one. Starting with the crudest scorer that works is the right Level-0 move.
 */
public final class Scorer {

    private Scorer() {
    }

    public static boolean passes(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        String e = expected.strip().toLowerCase();
        String a = actual.strip().toLowerCase();
        // Guard against an empty expected string, which would "contain-match" anything.
        return !e.isEmpty() && a.contains(e);
    }
}
