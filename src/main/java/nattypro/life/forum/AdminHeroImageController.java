package nattypro.life.forum;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;

@Controller
@RequestMapping("/admin/hero-images")
public class AdminHeroImageController {

    private final HeroImageService heroImageService;

    public AdminHeroImageController(HeroImageService heroImageService) {
        this.heroImageService = heroImageService;
    }

    @GetMapping
    public String managePage(Model model) {
        model.addAttribute("images", heroImageService.getAllImages());
        model.addAttribute("config", heroImageService.getOrCreateConfig());
        return "hero-images";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         @RequestParam(value = "caption", required = false) String caption,
                         RedirectAttributes ra) throws IOException {
        if (file.isEmpty()) {
            ra.addFlashAttribute("error", "Please select a file.");
            return "redirect:/admin/hero-images";
        }
        heroImageService.upload(file, caption);
        ra.addFlashAttribute("success", "Image uploaded.");
        return "redirect:/admin/hero-images";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        heroImageService.delete(id);
        ra.addFlashAttribute("success", "Image deleted.");
        return "redirect:/admin/hero-images";
    }

    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id) {
        heroImageService.toggleActive(id);
        return "redirect:/admin/hero-images";
    }

    @PostMapping("/move-up/{id}")
    public String moveUp(@PathVariable Long id) {
        heroImageService.moveUp(id);
        return "redirect:/admin/hero-images";
    }

    @PostMapping("/move-down/{id}")
    public String moveDown(@PathVariable Long id) {
        heroImageService.moveDown(id);
        return "redirect:/admin/hero-images";
    }

    @PostMapping("/config")
    public String saveConfig(@RequestParam("cycleMode") boolean cycleMode,
                             @RequestParam(value = "pinnedImageId", required = false) Long pinnedImageId,
                             RedirectAttributes ra) {
        heroImageService.saveConfig(cycleMode, pinnedImageId);
        ra.addFlashAttribute("success", "Settings saved.");
        return "redirect:/admin/hero-images";
    }
}