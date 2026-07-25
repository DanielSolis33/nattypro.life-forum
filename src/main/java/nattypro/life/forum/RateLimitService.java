package nattypro.life.forum;

import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean isAllowed(String key) {
        return buckets.computeIfAbsent(key, this::newBucket).tryConsume(1);
    }

    private Bucket newBucket(String key) {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(10).refillGreedy(10, Duration.ofMinutes(5)))
                .build();
    }
}