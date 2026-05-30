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

    // Video cache: channelId -> video info
    private final Map<String, Map<String, String>> videoCache = new ConcurrentHashMap<>();

    // Uploads playlist IDs never change — fetch once, keep forever
    private final Map<String, String> uploadsPlaylistCache = new ConcurrentHashMap<>();

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

    @Scheduled(fixedRate = 21600000) // every 6 hours
    public void refreshFeeds() {
        for (String channelId : FEATURED_CHANNELS) {
            try {
                fetchLatestVideo(channelId);
            } catch (Exception e) {
                System.err.println("Failed to fetch feed for channel: " + channelId + " - " + e.getMessage());
            }
        }
    }

    private String fetchUploadsPlaylistId(String channelId) {
        try {
            String url = "https://www.googleapis.com/youtube/v3/channels" +
                "?key=" + apiKey +
                "&id=" + channelId +
                "&part=contentDetails";

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("items")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
                if (!items.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> contentDetails = (Map<String, Object>) items.get(0).get("contentDetails");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> relatedPlaylists = (Map<String, Object>) contentDetails.get("relatedPlaylists");
                    String uploadsId = (String) relatedPlaylists.get("uploads");
                    System.out.println("Got uploads playlist for " + channelId + ": " + uploadsId);
                    return uploadsId;
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching uploads playlist for " + channelId + ": " + e.getMessage());
        }
        return null;
    }

    private void fetchLatestVideo(String channelId) {
        // Step 1: get uploads playlist ID (1 unit, cached permanently)
        String uploadsPlaylistId = uploadsPlaylistCache.computeIfAbsent(
            channelId, this::fetchUploadsPlaylistId);

        if (uploadsPlaylistId == null) {
            System.err.println("Could not get uploads playlist for: " + channelId);
            return;
        }

        // Step 2: get latest video from playlist (1 unit — replaces 100-unit search.list)
        String url = "https://www.googleapis.com/youtube/v3/playlistItems" +
            "?key=" + apiKey +
            "&playlistId=" + uploadsPlaylistId +
            "&part=snippet" +
            "&maxResults=1";

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("items")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

                if (!items.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> snippet = (Map<String, Object>) items.get(0).get("snippet");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> resourceId = (Map<String, Object>) snippet.get("resourceId");

                    String videoId = (String) resourceId.get("videoId");
                    if (videoId == null || videoId.isBlank()) {
                        System.err.println("No videoId for channel: " + channelId);
                        return;
                    }

                    @SuppressWarnings("unchecked")
                    Map<String, Object> thumbnails = (Map<String, Object>) snippet.get("thumbnails");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> thumbnail = (Map<String, Object>) thumbnails.getOrDefault("maxres",
                        thumbnails.getOrDefault("high", thumbnails.get("medium")));

                    // videoOwnerChannelTitle is the correct field in playlistItems
                    String channelTitle = (String) snippet.get("videoOwnerChannelTitle");
                    if (channelTitle == null) channelTitle = (String) snippet.get("channelTitle");

                    Map<String, String> videoInfo = new HashMap<>();
                    videoInfo.put("videoId", videoId);
                    videoInfo.put("title", (String) snippet.get("title"));
                    videoInfo.put("channelTitle", channelTitle);
                    videoInfo.put("thumbnail", (String) thumbnail.get("url"));
                    videoInfo.put("channelId", channelId);

                    videoCache.put(channelId, videoInfo);
                    System.out.println("Cached video for " + channelId + ": " + snippet.get("title"));
                }
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