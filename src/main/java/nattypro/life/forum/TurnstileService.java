package nattypro.life.forum;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TurnstileService {

    @Value("${cloudflare.turnstile.secret-key}")
    private String secretKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

public boolean verifyToken(String token) {
        if (token == null || token.isBlank()) {
            System.out.println("Turnstile: no token received from client (null or blank)");
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
            System.out.println("Turnstile siteverify response: " + response.body());
            JsonNode json = objectMapper.readTree(response.body());

            boolean success = json.path("success").asBoolean(false);
            System.out.println("Turnstile verification result: " + success);
            return success;
        } catch (Exception e) {
            System.err.println("Turnstile verification failed: " + e.getMessage());
            return false;
        }
    }
}