package com.yunlinwu.llmevalharness;

/**
 * One test case parsed out of {@code Eval.testCasesJson}.
 *
 * This is a Java <b>record</b> — a compact, immutable data carrier. The compiler
 * generates the constructor, getters ({@code input()}, {@code expectedOutput()}),
 * {@code equals}, {@code hashCode}, and {@code toString} for you. Spring Boot's
 * Jackson maps the JSON keys "input" and "expectedOutput" straight onto these two
 * components — no annotations needed.
 */
public record TestCase(String input, String expectedOutput) {
}
