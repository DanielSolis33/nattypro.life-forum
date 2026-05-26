package nattypro.life.forum;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class YouTubeFeedService {

    @Value("${youtube.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private final Map<String, Map<String, String>> videoCache = new ConcurrentHashMap<>();

    private static final List<String> FEATURED_CHANNELS = Arrays.asList(
        "UCDjSFOeyQ91aJoxTRqjaNWw",  // Longevity Muscle
        "UC8fhb7upVSZ0q-zK5snR9BA",  // 3DMJ
        "UC0SBUBfztKPNFI1hfS1668w",  // D.Sol Coaching
        "UCfm7KCNQMOq92nRbYs-0_FQ",  // Natty News Daily
        "UC6wB_e6YQncYgpv_QrMGHCQ",  // Revive Stronger
        "UCEGGAs257niPVJ5BvXymVLQ"   // Iron Culture
    );

    @PostConstruct
    public void initializeCache() {
        refreshFeeds();
    }

    @Scheduled(fixedRate = 21600000)
    public void refreshFeeds() {
        for (String channelId : FEATURED_CHANNELS) {
            try {
                fetchLatestVideo(channelId);
            } catch (Exception e) {
                System.err.println("Failed to fetch YouTube feed for channel: " + channelId + " - " + e.getMessage());
            }
        }
    }

    private void fetchLatestVideo(String channelId) {
        String url = "https://www.googleapis.com/youtube/v3/search" +
            "?key=" + apiKey +
            "&channelId=" + channelId +
            "&part=snippet" +
            "&order=date" +
            "&maxResults=1" +
            "&type=video";

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("items")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

                if (!items.isEmpty()) {
                    Map<String, Object> item = items.get(0);

                    @SuppressWarnings("unchecked")
                    Map<String, Object> id = (Map<String, Object>) item.get("id");

                    @SuppressWarnings("unchecked")
                    Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");

                    String videoId = (String) id.get("videoId");

                    // Guard: skip if videoId is missing (shouldn't happen with type=video, but just in case)
                    if (videoId == null || videoId.isBlank()) {
                        System.err.println("No videoId returned for channel: " + channelId + " — skipping. Full id object: " + id);
                        return;
                    }

                    @SuppressWarnings("unchecked")
                    Map<String, Object> thumbnails = (Map<String, Object>) snippet.get("thumbnails");

                    @SuppressWarnings("unchecked")
                    Map<String, Object> highThumbnail = (Map<String, Object>) thumbnails.getOrDefault("maxres", thumbnails.get("high"));

                    Map<String, String> videoInfo = new HashMap<>();
                    videoInfo.put("videoId", videoId);
                    videoInfo.put("title", (String) snippet.get("title"));
                    videoInfo.put("channelTitle", (String) snippet.get("channelTitle"));
                    videoInfo.put("thumbnail", (String) highThumbnail.get("url"));
                    videoInfo.put("channelId", channelId);

                    videoCache.put(channelId, videoInfo);
                    System.out.println("Cached video for " + channelId + ": videoId=" + videoId + " title=" + snippet.get("title"));
                } else {
                    System.err.println("No videos returned for channel: " + channelId);
                }
            } else {
                System.err.println("Unexpected API response for channel " + channelId + ": " + response);
            }
        } catch (Exception e) {
            System.err.println("YouTube API error for channel " + channelId + ": " + e.getMessage());
        }
    }

    public List<Map<String, String>> getLatestVideos() {
        return new ArrayList<>(videoCache.values());
    }

    public boolean hasCachedVideos() {
        return !videoCache.isEmpty();
    }
}