package nattypro.life.forum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ContentController {

    @Autowired private AnnouncementRepository announcementRepo;
    @Autowired private BannerRepository bannerRepo;
    @Autowired private ContactSubmissionRepository contactRepo;
    @Autowired private UserRepository userRepository;

    // ── Admin: Manage Announcements & Banners ──
    @GetMapping("/admin/content")
    public String adminContent(Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        if (!user.getRole().equals("ADMIN")) return "redirect:/";

        model.addAttribute("announcements", announcementRepo.findAll());
        model.addAttribute("banners", bannerRepo.findAll());
        model.addAttribute("contacts", contactRepo.findByIsReadFalseOrderBySubmittedAtDesc());
        return "admin-content";
    }

    // ── Add Announcement ──
    @PostMapping("/admin/announcements/add")
    public String addAnnouncement(@RequestParam String message, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        if (!user.getRole().equals("ADMIN")) return "redirect:/";

        Announcement a = new Announcement();
        a.setMessage(message);
        a.setCreatedBy(authentication.getName());
        announcementRepo.save(a);
        return "redirect:/admin/content";
    }

    // ── Toggle Announcement ──
    @PostMapping("/admin/announcements/toggle/{id}")
    public String toggleAnnouncement(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        if (!user.getRole().equals("ADMIN")) return "redirect:/";

        Announcement a = announcementRepo.findById(id).orElseThrow();
        a.setIsActive(!Boolean.TRUE.equals(a.getIsActive()));
        announcementRepo.save(a);
        return "redirect:/admin/content";
    }

    // ── Delete Announcement ──
    @PostMapping("/admin/announcements/delete/{id}")
    public String deleteAnnouncement(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        if (!user.getRole().equals("ADMIN")) return "redirect:/";
        announcementRepo.deleteById(id);
        return "redirect:/admin/content";
    }

    // ── Add Banner ──
    @PostMapping("/admin/banners/add")
    public String addBanner(@RequestParam String type,
                            @RequestParam(required = false) String youtubeVideoId,
                            @RequestParam(required = false) String customContent,
                            @RequestParam(required = false) String title,
                            @RequestParam(required = false) String link,
                            @RequestParam(required = false, defaultValue = "0") Integer displayOrder,
                            Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        if (!user.getRole().equals("ADMIN")) return "redirect:/";

        Banner b = new Banner();
        b.setType(type);
        b.setYoutubeVideoId(youtubeVideoId);
        b.setCustomContent(customContent);
        b.setTitle(title);
        b.setLink(link);
        b.setDisplayOrder(displayOrder);
        bannerRepo.save(b);
        return "redirect:/admin/content";
    }

    // ── Delete Banner ──
    @PostMapping("/admin/banners/delete/{id}")
    public String deleteBanner(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        if (!user.getRole().equals("ADMIN")) return "redirect:/";
        bannerRepo.deleteById(id);
        return "redirect:/admin/content";
    }

    // ── Contact Form Submission ──
    @PostMapping("/contact/submit")
    public String submitContact(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String subject,
                                @RequestParam String message) {
        ContactSubmission cs = new ContactSubmission();
        cs.setName(name);
        cs.setEmail(email);
        cs.setSubject(subject);
        cs.setMessage(message);
        contactRepo.save(cs);
        return "redirect:/?contacted=true";
    }

    // ── Mark contact as read ──
    @PostMapping("/admin/contacts/read/{id}")
    public String markRead(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        if (!user.getRole().equals("ADMIN")) return "redirect:/";
        ContactSubmission cs = contactRepo.findById(id).orElseThrow();
        cs.setIsRead(true);
        contactRepo.save(cs);
        return "redirect:/admin/content";
    }
}