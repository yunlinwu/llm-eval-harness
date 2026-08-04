package com.yunlinwu.llmevalharness;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration test for the /evals endpoints.
 *
 * The 20/80 patterns:
 *
 *   - {@code @SpringBootTest} — boot up the WHOLE Spring context for the test
 *     (all @Entity, @Repository, @RestController wired up, real H2 in-memory
 *     DB). This is slower than a unit test but far more truthful about how the
 *     code actually behaves in production.
 *   - {@code @AutoConfigureMockMvc} — give us a MockMvc bean that can send
 *     fake HTTP requests to our controllers without spinning up a real Tomcat
 *     port. Faster and more deterministic than real HTTP.
 *   - {@code MockMvc.perform(...).andExpect(...)} — the fluent assertion API
 *     for testing HTTP responses. Read top-to-bottom like a story.
 *
 * When your controller does more (auth, validation, service calls, external
 * API calls), you'll layer in @MockBean to stub out the parts you don't want
 * to hit in tests. For this MVP one integration test proves the whole stack
 * boots and the round-trip through H2 works — a strong "the plumbing works"
 * signal for the price of one test.
 */
@SpringBootTest
@AutoConfigureMockMvc
class EvalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createEval_returns201AndPersists() throws Exception {
        // Given: a request body that describes a small eval
        String body = """
                {
                  "name": "math basics",
                  "description": "Simple arithmetic sanity checks",
                  "testCasesJson": "[{\\"input\\":\\"2+2\\",\\"expectedOutput\\":\\"4\\"}]"
                }
                """;

        // When: we POST it to /evals
        // Then: the server responds 201 and echoes back the saved eval with a generated id
        mockMvc.perform(post("/evals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("math basics"));

        // And: the eval is now retrievable via GET /evals
        mockMvc.perform(get("/evals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("math basics"));
    }

    @Test
    void getMissingEval_returns404() throws Exception {
        mockMvc.perform(get("/evals/99999"))
                .andExpect(status().isNotFound());
    }
}
