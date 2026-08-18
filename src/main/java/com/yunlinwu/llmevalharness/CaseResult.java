package com.yunlinwu.llmevalharness;

/**
 * One test case's outcome after running: what we asked, what we expected, what
 * the model actually said, and whether it passed. A list of these is serialized
 * into {@code EvalRun.resultsJson} so you can inspect exactly what happened.
 */
public record CaseResult(String input, String expected, String actual, boolean passed) {
}
