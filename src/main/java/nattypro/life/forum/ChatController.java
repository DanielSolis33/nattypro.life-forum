package nattypro.life.forum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Spam detection — tracks message timestamps per user
    private final ConcurrentHashMap<String, List<LocalDateTime>> messageTimes = new ConcurrentHashMap<>();

    // Valid chat rooms
    private static final List<String> VALID_ROOMS = List.of(
        "global",
        "training",
        "nutrition",
        "recovery",
        "mindset",
        "prep-files",
        "coaches-corner",
        "self-promotion",
        "random-side-quests"
    );

    // ── Page handler ──
    @GetMapping("/chat/{room}")
    public String chatRoom(@PathVariable String room, Model model, Authentication authentication) {
        if (!VALID_ROOMS.contains(room)) {
            return "redirect:/";
        }

        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();

        // Check suspension
        if (Boolean.TRUE.equals(user.getChatSuspended())) {
            model.addAttribute("error", "Your chat access has been suspended.");
            return "chat-locked";
        }

        // Check mute
        if (user.getChatMutedUntil() != null && LocalDateTime.now().isBefore(user.getChatMutedUntil())) {
            model.addAttribute("mutedUntil", user.getChatMutedUntil()
                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")));
            return "chat-muted";
        }

        model.addAttribute("room", room);
        model.addAttribute("username", authentication.getName());
        model.addAttribute("rules", getChatRules());
        return "chat";
    }

    // ── WebSocket message handler ──
    @MessageMapping("/chat/{room}")
    @SendTo("/topic/chat/{room}")
    public Map<String, String> sendMessage(
            @DestinationVariable String room,
            @Payload Map<String, String> message,
            Principal principal) {

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        // Check suspension
        if (Boolean.TRUE.equals(user.getChatSuspended())) {
            return Map.of("type", "error", "content", "Your account has been suspended.");
        }

        // Check mute
        if (user.getChatMutedUntil() != null && LocalDateTime.now().isBefore(user.getChatMutedUntil())) {
            return Map.of("type", "error", "content", "You are currently muted.");
        }

        // Spam detection
        String spamResult = checkSpam(username, user);
        if (spamResult != null) {
            userRepository.save(user);
            return Map.of("type", "error", "content", spamResult);
        }

        // Sanitize message — strip URLs and HTML
        String content = sanitize(message.get("content"));

        if (content == null || content.isBlank()) {
            return Map.of("type", "error", "content", "Empty message.");
        }

        return Map.of(
            "type", "message",
            "sender", username,
            "content", content,
            "room", room,
            "time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))
        );
    }

    // ── Spam detection ──
    private String checkSpam(String username, User user) {
        messageTimes.putIfAbsent(username, new ArrayList<>());
        List<LocalDateTime> times = messageTimes.get(username);

        LocalDateTime now = LocalDateTime.now();
        // Remove timestamps older than 10 seconds
        times.removeIf(t -> t.isBefore(now.minusSeconds(10)));
        times.add(now);

        if (times.size() >= 5) {
            times.clear();

            // Escalating — already been muted before?
            if (user.getChatMutedUntil() != null) {
                // Second offence — suspend
                user.setChatSuspended(true);
                return "Your account has been suspended for repeated spam violations.";
            } else {
                // First offence — mute 30 min
                user.setChatMutedUntil(LocalDateTime.now().plusMinutes(30));
                return "You have been muted for 30 minutes due to spamming.";
            }
        }
        return null;
    }

    // ── Sanitize message content ──
    private String sanitize(String content) {
        if (content == null) return null;
        // Strip URLs
        content = content.replaceAll("(https?://|www\\.)\\S+", "[link removed]");
        // Strip HTML tags
        content = content.replaceAll("<[^>]*>", "");
        // Trim
        return content.trim();
    }

    // ── Chat rules ──
    private List<String> getChatRules() {
        return List.of(
            "Be respectful — no personal attacks or harassment.",
            "No spamming — sending too many messages will result in a mute.",
            "No marketing — direct or indirect promotion is not allowed.",
            "No links or images — these are automatically removed.",
            "Keep it natural — this is a drug-free community.",
            "Violations may result in a mute or account suspension."
        );
    }
}