package com.yunlinwu.llmevalharness;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class handles the HTTP side. Every method here maps a URL + HTTP verb to
 * a Java method — Spring turns the request body into an object for us (via
 * Jackson), and turns whatever we return back into a JSON response.
 *
 * The 20/80 patterns:
 *
 *   - {@code @RestController} — "this class handles HTTP and returns data,
 *     not HTML." (There's also {@code @Controller} for server-rendered pages,
 *     but @RestController is what you want for JSON APIs, which is most of
 *     modern Spring Boot work.)
 *   - {@code @RequestMapping("/evals")} — every method in this class is
 *     rooted at /evals. Saves us from repeating the prefix on each mapping.
 *   - {@code @PostMapping} / {@code @GetMapping} — HTTP verb + subpath.
 *   - {@code @RequestBody} — parse the JSON body into this parameter.
 *   - {@code @PathVariable} — pull a value out of the URL like /evals/42.
 *   - {@code ResponseEntity} — lets us control the HTTP status code + body
 *     together. When we return a plain object, Spring assumes 200 OK.
 *
 * Constructor injection: we take {@code EvalRepository} as a constructor
 * parameter. Spring sees this at startup and passes the implementation it
 * generated. This is called dependency injection — you don't {@code new}
 * things; Spring wires them for you. Constructor injection (vs. field
 * injection with @Autowired) is the modern-Spring recommended way because it
 * makes dependencies explicit and easy to mock in tests.
 *
 * Deferred to a future session: a Service layer (a dedicated class between
 * Controller and Repository for business logic). Adding a Service becomes
 * useful once you have multi-step operations, transactions across multiple
 * repositories, or logic you want to share between endpoints. For this MVP,
 * the controller talks straight to the repository — the "boring but real"
 * choice.
 */
@RestController
@RequestMapping("/evals")
public class EvalController {

    private final EvalRepository evalRepository;

    public EvalController(EvalRepository evalRepository) {
        this.evalRepository = evalRepository;
    }

    /**
     * Create a new eval.
     *
     * Example:
     *   curl -X POST http://localhost:8080/evals \
     *     -H 'Content-Type: application/json' \
     *     -d '{"name":"math basics",
     *          "description":"Simple arithmetic",
     *          "testCasesJson":"[{\"input\":\"2+2\",\"expectedOutput\":\"4\"}]"}'
     */
    @PostMapping
    public ResponseEntity<Eval> create(@RequestBody Eval eval) {
        Eval saved = evalRepository.save(eval);
        return ResponseEntity.status(201).body(saved);
    }

    /** List every eval. Useful for a "which evals do I have?" UI. */
    @GetMapping
    public List<Eval> list() {
        return evalRepository.findAll();
    }

    /** Fetch one eval by id. 404 if it does not exist. */
    @GetMapping("/{id}")
    public ResponseEntity<Eval> get(@PathVariable Long id) {
        return evalRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
