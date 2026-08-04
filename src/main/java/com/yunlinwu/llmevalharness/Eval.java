package com.yunlinwu.llmevalharness;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

/**
 * An "Eval" is a named collection of test cases you want to run against an LLM.
 *
 * This class is our first {@code @Entity}. In JPA-speak, an @Entity is a plain
 * Java class that JPA/Hibernate turns into a database table for us. We don't
 * write any SQL to create the table — Hibernate reads these annotations and
 * emits {@code CREATE TABLE eval (...)} on startup.
 *
 * The 20/80 patterns you're seeing:
 *
 *   - {@code @Entity} — this class is a table.
 *   - {@code @Id} + {@code @GeneratedValue} — the primary key is auto-assigned
 *     by the database, we don't set it ourselves.
 *   - {@code @Column} + {@code @Lob} — override the default column shape when
 *     the defaults aren't right (here: we want a big TEXT column for JSON).
 *   - JPA needs a public no-arg constructor + getters/setters. That's why you
 *     see the default constructor + all the accessors — Hibernate uses them to
 *     rehydrate rows from the database.
 *
 * What we're intentionally NOT doing tonight (esoterica for future sessions):
 *   - Modeling test cases as a separate @Entity with @OneToMany. Instead we
 *     store them as embedded JSON in one column, which is way simpler and
 *     enough for this use case.
 *   - A separate DTO layer. This class is both our JPA entity and our REST
 *     request/response shape. In real projects you split them once the two
 *     diverge — until then, one class is fine.
 *   - Lombok. Java's getter/setter boilerplate is annoying; Lombok removes it.
 *     Skipped for now to keep the "no magic" story clean.
 */
@Entity
public class Eval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    // Test cases live here as a JSON string — see the class-level comment for why.
    // Example value:
    //   [{"input":"What is 2+2?","expectedOutput":"4"},
    //    {"input":"Capital of France","expectedOutput":"Paris"}]
    @Lob
    @Column(name = "test_cases_json", columnDefinition = "TEXT")
    private String testCasesJson;

    // JPA needs this no-arg constructor to build an instance before setting fields.
    public Eval() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTestCasesJson() { return testCasesJson; }
    public void setTestCasesJson(String testCasesJson) { this.testCasesJson = testCasesJson; }
}
