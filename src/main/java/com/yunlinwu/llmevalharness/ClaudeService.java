package com.yunlinwu.llmevalharness;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;

/**
 * Talks to Claude. Isolated behind this one service so the rest of the app never
 * needs to know which LLM provider we use or how the SDK works — it just calls
 * {@link #complete(String, String)} and gets back text.
 *
 * Uses the official Anthropic Java SDK. The SDK reads your key from the
 * {@code ANTHROPIC_API_KEY} environment variable — the key never appears in code
 * or config. Set it in your shell before running a real eval:
 * <pre>export ANTHROPIC_API_KEY=sk-ant-...</pre>
 *
 * 20/80 note: this is the "call an external service" pattern. One method in, one
 * String out; everything provider-specific lives behind this door.
 */
@Service
public class ClaudeService {

    private final String model;

    // Built lazily on first use. Constructing the SDK client calls fromEnv(),
    // which needs the API key — deferring it means the app (and the tests) start
    // fine without a key, and only an actual eval run requires one.
    private AnthropicClient client;

    public ClaudeService(@Value("${anthropic.model}") String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    /** Send a system prompt + a user message to Claude; return the reply text. */
    public String complete(String systemPrompt, String userInput) {
        MessageCreateParams params = MessageCreateParams.builder()
                .model(model)
                .maxTokens(1024L)
                .system(systemPrompt)
                .addUserMessage(userInput)
                .build();

        Message response = client().messages().create(params);

        // A response is a list of content blocks; concatenate the text of the
        // text blocks (a simple answer is a single text block).
        return response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(textBlock -> textBlock.text())
                .collect(Collectors.joining());
    }

    private synchronized AnthropicClient client() {
        if (client == null) {
            client = AnthropicOkHttpClient.fromEnv();
        }
        return client;
    }
}
