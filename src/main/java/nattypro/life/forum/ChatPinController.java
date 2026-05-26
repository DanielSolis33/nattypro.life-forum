package nattypro.life.forum;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChatPinController {

    @Autowired private ChatInvitePinRepository pinRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserService userService;

    // ── PIN entry page ──
    @GetMapping("/chat/unlock")
    public String showPinPage() {
        return "chat-unlock";
    }

    @PostMapping("/chat/unlock")
    public String submitPin(@RequestParam String pin,
                            Authentication authentication,
                            Model model) {

        ChatInvitePin invitePin = pinRepository.findByPin(pin).orElse(null);

        if (invitePin == null || Boolean.TRUE.equals(invitePin.getIsUsed())) {
            model.addAttribute("error", "Invalid or already used PIN.");
            return "chat-unlock";
        }

        // Mark PIN as used
        invitePin.setIsUsed(true);
        invitePin.setUsedBy(authentication.getName());
        invitePin.setUsedAt(LocalDateTime.now());
        pinRepository.save(invitePin);

        // Grant chat access to user
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        user.setChatAccess(true);
        userRepository.save(user);

        return "redirect:/chat/global";
    }

    // ── Admin PIN management ──
    @GetMapping("/admin/pins")
    public String adminPins(Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        if (!user.getRole().equals("ADMIN")) {
            return "redirect:/";
        }
        model.addAttribute("pins", pinRepository.findAllByOrderByCreatedAtDesc());
        return "admin-pins";
    }

    @PostMapping("/admin/pins/generate")
    public String generatePin(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        if (!user.getRole().equals("ADMIN")) {
            return "redirect:/";
        }

        ChatInvitePin pin = new ChatInvitePin();
        pin.setPin(UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        pin.setCreatedBy(authentication.getName());
        pinRepository.save(pin);

        return "redirect:/admin/pins";
    }
    @GetMapping("/admin/users")
    public String adminUsers(Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        if (!user.getRole().equals("ADMIN")) {
            return "redirect:/";
        }
        model.addAttribute("users", userRepository.findAll().stream()
        	    .filter(u -> !u.getUsername().startsWith("DeletedUser_"))
        	    .collect(java.util.stream.Collectors.toList()));
        return "admin-users";
    }

    @PostMapping("/admin/users/togglePro/{username}")
    public String toggleProBadge(@PathVariable String username, Authentication authentication) {
        User admin = userRepository.findByUsername(authentication.getName()).orElseThrow();
        if (!admin.getRole().equals("ADMIN")) {
            return "redirect:/";
        }
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setVerifiedNattyPro(!Boolean.TRUE.equals(user.getVerifiedNattyPro()));
        userRepository.save(user);
        return "redirect:/admin/users";
    }
    @PostMapping("/admin/users/delete/{username}")
    public String adminDeleteUser(@PathVariable String username, Authentication authentication) {
        User admin = userRepository.findByUsername(authentication.getName()).orElseThrow();
        if (!admin.getRole().equals("ADMIN")) return "redirect:/";

        // Prevent admin from deleting themselvesO
        if (username.equals(authentication.getName())) {
            return "redirect:/admin/users?error=cannotDeleteSelf";
        }

        User user = userRepository.findByUsername(username).orElseThrow();
        userService.anonymizeUser(user.getId());
        return "redirect:/admin/users?deleted=true";
    }
}