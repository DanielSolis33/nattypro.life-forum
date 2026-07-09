package nattypro.life.forum;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;


@Controller
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private EmailService emailService;
    @Autowired private TurnstileService turnstileService;
    
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    // ── Step 1: Submit registration → store in session, go to age check ──
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String email,
                           @RequestParam(name = "cf-turnstile-response", required = false) String turnstileToken,
                           HttpSession session,
                           Model model) {
        // Verify Turnstile token before touching the database
       if (!turnstileService.verifyToken(turnstileToken)) {
           model.addAttribute("error", "CAPTCHA verification failed. Please refresh and try again.");
           return "register";
      }
        
                            // Check if username/email already exists before going further
        try {
            userService.validateNewUser(username, email);
        } catch (Exception e) {
            model.addAttribute("error", "Username or email already exists");
            return "register";
        }

        // Store in session temporarily — don't create user yet
        session.setAttribute("reg_username", username);
        session.setAttribute("reg_password", password);
        session.setAttribute("reg_email", email);

        return "redirect:/register/age";
    }

    // ── Step 2: Age verification ──
    @GetMapping("/register/age")
    public String showAgePage(HttpSession session) {
        if (session.getAttribute("reg_username") == null) {
            return "redirect:/register";
        }
        return "register-age";
    }

    @PostMapping("/register/age")
    public String submitAge(@RequestParam String ageConfirm,
                            HttpSession session,
                            Model model) {
        if (session.getAttribute("reg_username") == null) {
            return "redirect:/register";
        }

        if (!ageConfirm.equals("yes")) {
            return "register-blocked";
        }

        return "redirect:/register/rules";
    }

    // ── Step 3: Rules & Mission ──
    @GetMapping("/register/rules")
    public String showRulesPage(HttpSession session) {
        if (session.getAttribute("reg_username") == null) {
            return "redirect:/register";
        }
        return "register-rules";
    }

    @PostMapping("/register/rules")
    public String submitRules(@RequestParam(required = false) String acceptRules,
                              HttpSession session,
                              Model model) {
        if (session.getAttribute("reg_username") == null) {
            return "redirect:/register";
        }

        if (acceptRules == null) {
            model.addAttribute("error", "You must accept the community guidelines to register.");
            return "register-rules";
        }

        // All checks passed — create the user
        String username = (String) session.getAttribute("reg_username");
        String password = (String) session.getAttribute("reg_password");
        String email    = (String) session.getAttribute("reg_email");

        try {
            User newUser = userService.registerUser(username, password, email);
            newUser.setAcceptedRules(true);
            userRepository.save(newUser);
        } catch (Exception e) {
            System.err.println("Registration failed: " + e.getMessage());
            // Clean up any partially created user
            userRepository.findByUsername(username).ifPresent(userRepository::delete);
            model.addAttribute("error", "Something went wrong. Please try again.");
            return "register-rules";
        }
        
        // Clear session
        session.removeAttribute("reg_username");
        session.removeAttribute("reg_password");
        session.removeAttribute("reg_email");
        
     // Send email confirmation
     // Send email confirmation
        try {
            String confirmToken = UUID.randomUUID().toString();
            User newUser = userRepository.findByUsername(username).orElseThrow();
            newUser.setConfirmationToken(confirmToken);
            userRepository.save(newUser);
            emailService.sendEmailConfirmation(email, username, confirmToken);
        } catch (Exception e) {
            System.err.println("Failed to send confirmation email: " + e.getMessage());
        }  // ← close catch here

        return "redirect:/login?registered=true";  // ← then return here
    }
    

    // ── Login ──
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
    
    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "privacy-policy";
    }

    @GetMapping("/terms-of-service")
    public String termsOfService() {
        return "terms-of-service";
    }
    @GetMapping("/community-guidelines")
    public String communityGuidelines() {
        return "community-guidelines";
    }
    @GetMapping("/sponsors")
    public String sponsors() {
        return "sponsors";
    }
}