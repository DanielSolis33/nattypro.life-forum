package nattypro.life.forum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class PasswordResetController {

    @Autowired private UserRepository userRepository;
    @Autowired private EmailService emailService;
    @Autowired private PasswordEncoder passwordEncoder;

    // ── Forgot Password Page ──
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model) {
        User user = userRepository.findByEmail(email).orElse(null);

        // Always show success to prevent email enumeration attacks
        if (user != null) {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            emailService.sendPasswordResetEmail(email, token);
        }

        model.addAttribute("message", "If that email exists, a reset link has been sent.");
        return "forgot-password";
    }

    // ── Reset Password Page ──
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        User user = userRepository.findByResetToken(token).orElse(null);

        if (user == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "This reset link is invalid or has expired.");
            return "reset-password";
        }

        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token,
                                       @RequestParam String password,
                                       @RequestParam String confirmPassword,
                                       Model model) {
        User user = userRepository.findByResetToken(token).orElse(null);

        if (user == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "This reset link is invalid or has expired.");
            return "reset-password";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("token", token);
            return "reset-password";
        }

        if (password.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters.");
            model.addAttribute("token", token);
            return "reset-password";
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return "redirect:/login?passwordReset=true";
    }

    // ── Email Confirmation ──
    @GetMapping("/confirm-email")
    public String confirmEmail(@RequestParam String token, Model model) {
        User user = userRepository.findByConfirmationToken(token).orElse(null);

        if (user == null) {
            model.addAttribute("error", "Invalid confirmation link.");
            return "confirm-email";
        }

        user.setEmailConfirmed(true);
        user.setConfirmationToken(null);
        userRepository.save(user);

        model.addAttribute("success", true);
        return "confirm-email";
    }
}