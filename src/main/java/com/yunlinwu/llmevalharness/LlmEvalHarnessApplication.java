package com.yunlinwu.llmevalharness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point. Every Spring Boot app has exactly one class annotated with
 * {@code @SpringBootApplication} — it does three things at once:
 *
 *   1. Marks this package (and everything under it) as scannable for other
 *      Spring components — every {@code @RestController}, {@code @Service},
 *      {@code @Entity}, etc. gets auto-discovered.
 *   2. Turns on auto-configuration: Spring looks at what's on the classpath
 *      (we have spring-boot-starter-web + JPA + H2), and wires up sane defaults
 *      (Tomcat on port 8080, an in-memory H2 datasource, Hibernate managing our
 *      entities). This is the biggest single win in Spring Boot vs. old-Spring.
 *   3. Marks the class itself as a Spring configuration class.
 *
 * You almost never edit this file after creating it. That is the 20/80.
 */
@SpringBootApplication
public class LlmEvalHarnessApplication {

    public static void main(String[] args) {
        SpringApplication.run(LlmEvalHarnessApplication.class, args);
    }
}
