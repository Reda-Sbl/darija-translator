package ma.aui.translator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Calls the Gemini REST API to translate text into Darija, with retries and a backup model. */
public class GeminiService {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();
    private static final int MAX_ATTEMPTS = 3;

    public String translate(String text, String source) throws IOException, InterruptedException {
        String apiKey = Config.get("gemini.api.key");
        if (apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is not configured");
        }
        String main = Config.get("gemini.model");
        String fallback = Config.get("gemini.fallback.model");

        try {
            return translateWithRetry(main, apiKey, text, source);
        } catch (IOException e) {
            if (fallback.isBlank() || fallback.equals(main)) throw e;
            return translateWithRetry(fallback, apiKey, text, source);
        }
    }

    private String translateWithRetry(String model, String apiKey, String text, String source)
            throws IOException, InterruptedException {
        IOException last = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return callGemini(model, apiKey, text, source);
            } catch (RetryableException e) {          // 429 / 5xx / timeout: wait and retry
                last = new IOException(e.getMessage());
                if (attempt < MAX_ATTEMPTS) Thread.sleep(1500L * attempt);
            }
        }
        throw last;
    }

    private String callGemini(String model, String apiKey, String text, String source)
            throws IOException, InterruptedException {
        String target = Config.get("target.language");
        String instruction = "You are a professional translator. Translate the user's text"
                + (source == null || source.isBlank() ? "" : " from " + source)
                + " into " + target + ", written in Arabic script. "
                + "Reply with ONLY the translation, no explanations, no quotes.";

        ObjectNode body = MAPPER.createObjectNode();
        body.putObject("system_instruction").putArray("parts").addObject().put("text", instruction);
        ArrayNode contents = body.putArray("contents");
        contents.addObject().putArray("parts").addObject().put("text", text);
        body.putObject("generationConfig").put("temperature", 0.2);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/"
                        + model + ":generateContent"))
                .timeout(Duration.ofSeconds(45))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                .build();

        HttpResponse<String> res;
        try {
            res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (java.net.http.HttpTimeoutException e) {
            throw new RetryableException("Gemini (" + model + ") timed out");
        }
        int code = res.statusCode();
        if (code == 429 || code >= 500) {
            throw new RetryableException("Gemini (" + model + ") returned HTTP " + code + " (busy)");
        }
        if (code != 200) {
            throw new IOException("Gemini returned HTTP " + code + ": " + res.body());
        }
        JsonNode root = MAPPER.readTree(res.body());
        JsonNode out = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (out.isMissingNode()) {
            throw new IOException("Unexpected Gemini response: " + res.body());
        }
        return out.asText().trim();
    }

    private static class RetryableException extends IOException {
        RetryableException(String msg) { super(msg); }
    }
}
