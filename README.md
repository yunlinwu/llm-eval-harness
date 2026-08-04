# llm-eval-harness

A tiny Spring Boot service for defining and running LLM evaluations.

Companion to [transcript-rag](https://github.com/yunlinwu/transcript-rag): where
that repo teaches RAG by building it top-to-bottom in ~150 legible lines of
Python, this one teaches Spring Boot by building an eval harness the same way —
one small pattern at a time, no framework hiding the mechanism.

## Why this exists

Every AI job description I read asks about **evaluations**: how do you know a
model change was actually better? Which model do you trust for which task?
Almost no early-career portfolio has a real answer. The transcript-rag repo
has grounded generation with citation guardrails, but no eval story — so I'm
building one.

Second motivation: to actually learn Spring Boot fundamentals, not just skim
them. I'm doing this by shipping a real thing in the framework, one 20/80
pattern per evening.

## What the finished project will do (roadmap)

- **Define an eval** — name, description, test cases (input + expected output)
- **Run an eval** — pick an eval and an LLM provider, execute each test case
- **Score the run** — start with exact-match, add fuzzy-match and LLM-judge later
- **Store and compare runs** — was the new prompt/model better than the last?
- **Optional Angular frontend** — for the interview-story version

## Current state (Evening 1)

Just the "define an eval" slice — REST CRUD backed by H2 in-memory storage.
Runs, scoring, and provider integration come next.

Implemented:
- `POST /evals` — create an eval
- `GET /evals` — list all evals
- `GET /evals/{id}` — fetch one eval
- One integration test that proves the round-trip works

## The 5 Spring Boot patterns this evening teaches

Read the code files top-to-bottom in this order — each one has a header
comment explaining the pattern and why it exists.

1. **`LlmEvalHarnessApplication.java`** — `@SpringBootApplication`, the entry
   point that turns on component scanning + auto-configuration.
2. **`Eval.java`** — `@Entity` + JPA annotations, how a Java class becomes
   a database table.
3. **`EvalRepository.java`** — `JpaRepository`, the interface-with-no-code
   trick that gives you free CRUD.
4. **`EvalController.java`** — `@RestController` + `@RequestMapping` +
   `@PostMapping`/`@GetMapping`, how HTTP methods become Java methods.
5. **`EvalControllerTest.java`** — `@SpringBootTest` + `MockMvc`, how to
   test the whole stack without spinning up a real HTTP server.

## Deliberately skipped for later (so you know what you're not learning yet)

- **Service layer / DTOs / mappers.** The controller talks to the repository
  directly right now. This is fine while the entity == the API shape; you add
  layers when they diverge or when logic gets shared across endpoints.
- **Lombok.** Cuts getter/setter boilerplate but adds "magic." Left off so the
  patterns are legible.
- **@OneToMany between Eval and TestCase.** Test cases are embedded JSON for
  now — one less relationship to model. Split into a real entity when the runs
  feature needs individual test-case results.
- **Auth, validation, error handling, exception mappers.** Real production
  concerns; not the point of Evening 1.
- **Real LLM provider integration** (Claude, OpenAI). Coming Evening 2.
- **Runs + scoring.** Coming Evenings 2–3.
- **Angular frontend.** Coming later, if it earns its keep for the interview
  story.

## Run it locally

Prereqs: **Java 17+** and **Maven** installed. (On macOS:
`brew install openjdk@17 maven`.)

```bash
mvn spring-boot:run
```

App starts on `http://localhost:8080`.

Try it:

```bash
# Create an eval
curl -X POST http://localhost:8080/evals \
  -H 'Content-Type: application/json' \
  -d '{
    "name":"math basics",
    "description":"Simple arithmetic sanity checks",
    "testCasesJson":"[{\"input\":\"2+2\",\"expectedOutput\":\"4\"}]"
  }'

# List evals
curl http://localhost:8080/evals

# Fetch one
curl http://localhost:8080/evals/1
```

Poke the DB directly at `http://localhost:8080/h2-console` (JDBC URL:
`jdbc:h2:mem:evals`, user: `sa`, no password).

## Run the tests

```bash
mvn test
```

## Roadmap

- **Evening 1 (this commit):** Eval CRUD + H2 + one test.
- **Evening 2:** Add a Run entity + `POST /evals/{id}/runs` that actually calls
  Claude on each test case; store results.
- **Evening 3:** Add scoring — start with exact-match, add fuzzy-match and
  LLM-judge scorers.
- **Evening 4+:** Second provider (OpenAI), run comparison, maybe Angular
  frontend if it adds interview-story value.

## License

MIT — learn from it, fork it, use it in your own interview prep.
