package nattypro.life.forum;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TurnstileService {

    private static final Logger logger = LoggerFactory.getLogger(TurnstileService.class);

    @Value("${cloudflare.turnstile.secret-key}")
    private String secretKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

public boolean verifyToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            String body = "secret=" + secretKey + "&response=" + token;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://challenges.cloudflare.com/turnstile/v0/siteverify"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(Duration.ofSeconds(5))
                    .POST(BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = objectMapper.readTree(response.body());

       boolean success = json.path("success").asBoolean(false);
            return success;
        } catch (IOException | InterruptedException e) {
            logger.error("Turnstile verification failed", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }
}