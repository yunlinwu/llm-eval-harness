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

## Current state (Evening 2)

It's now a **real eval harness**: define evals, then actually run them against
Claude and score the answers.

Implemented:
- `POST /evals` — create an eval
- `GET /evals` — list all evals
- `GET /evals/{id}` — fetch one eval
- **`POST /evals/{id}/runs`** — run the eval: call Claude once per test case,
  score each answer, store the results
- **`GET /runs/{id}`** — fetch a past run (summary + per-case results)
- 8 tests (2 web integration + 6 pure scorer unit tests)

## Spring Boot patterns, by evening

Read the code files top-to-bottom — each has a header comment explaining the
pattern and why it exists.

**Evening 1 — the 5 core patterns:**

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

**Evening 2 — calling an LLM and scoring (new patterns):**

6. **`ClaudeService.java`** — `@Service` + the official Anthropic Java SDK: the
   "call an external API" pattern, isolated behind one method. Reads the key
   from `ANTHROPIC_API_KEY` (never in code).
7. **`EvalRunnerService.java`** — the *second* `@Service`, and the one that
   earns the layer: load → parse JSON → call the LLM N times → score →
   summarize → save. Uses Spring's own `ObjectMapper` (Jackson) via constructor
   injection. This is *when* you add a service — the logic outgrew the controller.
8. **`Scorer.java`** — a pure static function that decides pass/fail (normalized
   "contains" match). Pure → unit-testable with no API calls (see `ScorerTest`).
9. **`EvalRun.java` / `EvalRunRepository.java`** — a second `@Entity` + repo,
   reinforcing Evening 1's pattern; `evalId` is a plain foreign-key field, not
   a `@ManyToOne` (kept simple on purpose).
10. **`RunController.java`** — a second `@RestController`, split by resource
    (evals vs runs) — the conventional layout once you have more than one.
11. **`TestCase.java` / `CaseResult.java`** — Java **records**: compact
    immutable data carriers Jackson maps JSON onto for free.

## Deliberately skipped for later (so you know what you're not learning yet)

- **Service layer / DTOs / mappers.** The controller talks to the repository
  directly right now. This is fine while the entity == the API shape; you add
  layers when they diverge or when logic gets shared across endpoints.
- **Lombok.** Cuts getter/setter boilerplate but adds "magic." Left off so the
  patterns are legible.
- **@OneToMany between Eval and TestCase.** Test cases are embedded JSON for
  now — one less relationship to model. Split into a real entity when the runs
  feature needs individual test-case results.
- **Angular frontend.** Coming later, if it earns its keep for the interview
  story.
- **More scorers** — exact-match and an "LLM-as-judge" (ask Claude to grade the
  answer). The `Scorer` is a pure function, so new scorers slot in beside it.
- **`@OneToMany` between Eval and TestCase / EvalRun.** Still embedded JSON —
  fine until you want to query individual cases.
- **DTOs, auth, richer validation, exception mappers.** Production concerns; not
  the point yet.

## Run it locally

Prereqs: **Java 17+**, **Maven**, and **Docker Desktop**. (On macOS:
`brew install openjdk@17 maven`; Docker Desktop from docker.com.)

Start Postgres first (defined in `docker-compose.yml`; data lives in a named
volume, so it survives restarts):

```bash
docker compose up -d
```

Running an eval calls the Claude API, so set your key first (the SDK reads it
from the environment — it never goes in code or config):

```bash
export ANTHROPIC_API_KEY=sk-ant-...
mvn spring-boot:run
```

App starts on `http://localhost:8080`. The model under test is
`anthropic.model` in `application.properties` (currently `claude-haiku-5-5`,
cheap and fast while iterating).

Full loop — define an eval, then run it:

```bash
# 1. Create an eval
curl -X POST http://localhost:8080/evals \
  -H 'Content-Type: application/json' \
  -d '{
    "name":"math + geography",
    "description":"basic sanity checks",
    "testCasesJson":"[{\"input\":\"What is 2+2?\",\"expectedOutput\":\"4\"},{\"input\":\"Capital of France?\",\"expectedOutput\":\"Paris\"}]"
  }'

# 2. Run it against the model (calls Claude, spends a few tokens)
curl -X POST http://localhost:8080/evals/1/runs

# → {"id":1,"evalId":1,"model":"claude-opus-5","totalCount":2,"passedCount":2,
#    "resultsJson":"[{\"input\":\"What is 2+2?\",\"expected\":\"4\",\"actual\":\"4\",\"passed\":true}, ...]", ...}

# 3. Fetch the run again later
curl http://localhost:8080/runs/1
```

Poke the DB directly with `psql` inside the container:

```bash
docker compose exec postgres psql -U evals -d evals
# evals=# \dt                 list tables (eval, eval_run)
# evals=# SELECT * FROM eval;  see your rows
# evals=# \q                  quit
```

Stop Postgres with `docker compose down` (data kept) or `docker compose down -v`
(data wiped).

## Run the tests

```bash
mvn test
```

The tests need **no API key and no Docker** — `@AutoConfigureTestDatabase`
swaps Postgres for in-memory H2 in the integration test, the scorer is tested as a pure function and the
web tests only exercise CRUD. The LLM call is left for manual runs on purpose:
you don't want a test suite spending tokens or depending on the network.

## Roadmap

- **Evening 1:** Eval CRUD + H2 + one test.
- **Evening 2:** `ClaudeService` + `EvalRunnerService` +
  `Scorer` + `EvalRun`; `POST /evals/{id}/runs` calls Claude per case and scores it.
- **Evening 3:** Mockito unit tests for `EvalRunnerService`.
- **Evening 4:** PostgreSQL in Docker Compose replaces H2 for the app; tests
  stay on H2.
- **Next:** More scorers (exact-match, LLM-as-judge); compare runs.
- **Evening 4+:** Second provider, run comparison, maybe an Angular frontend if
  it adds interview-story value.

## License

MIT — learn from it, fork it, use it in your own interview prep.
