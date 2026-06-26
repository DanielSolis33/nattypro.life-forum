package nattypro.life.forum;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class HeroImageService {

    private final HeroImageRepository heroRepo;
    private final HeroCarouselConfigRepository configRepo;
    private final S3Service s3Service;

    public HeroImageService(HeroImageRepository heroRepo,
                            HeroCarouselConfigRepository configRepo,
                            S3Service s3Service) {
        this.heroRepo   = heroRepo;
        this.configRepo = configRepo;
        this.s3Service  = s3Service;
    }

    // ── Called by HomeController ──────────────────────────────────────────────

    public List<HeroImage> getActiveSlides() {
        HeroCarouselConfig cfg = getOrCreateConfig();
        if (Boolean.TRUE.equals(cfg.getCycleMode())) {
            return heroRepo.findByIsActiveTrueOrderByDisplayOrderAsc();
        }
        Long pinned = cfg.getPinnedImageId();
        if (pinned == null) return Collections.emptyList();
        return heroRepo.findById(pinned).map(List::of).orElse(Collections.emptyList());
    }

    // ── Called by AdminHeroImageController ───────────────────────────────────

    public List<HeroImage> getAllImages() {
        return heroRepo.findAllByOrderByDisplayOrderAsc();
    }

    public HeroCarouselConfig getOrCreateConfig() {
        return configRepo.findAll().stream()
            .findFirst()
            .orElseGet(() -> configRepo.save(new HeroCarouselConfig()));
    }

    public HeroImage upload(MultipartFile file, String caption) throws IOException {
        String url = s3Service.uploadHeroImage(file);
        int nextOrder = heroRepo.findAllByOrderByDisplayOrderAsc().size();
        HeroImage img = new HeroImage();
        img.setS3Url(url);
        img.setCaption(caption);
        img.setDisplayOrder(nextOrder);
        img.setIsActive(true);
        return heroRepo.save(img);
    }

    public void delete(Long id) {
        heroRepo.findById(id).ifPresent(img -> {
            s3Service.deleteFile(img.getS3Url());
            heroRepo.delete(img);
        });
    }

    public void toggleActive(Long id) {
        heroRepo.findById(id).ifPresent(img -> {
            img.setIsActive(!Boolean.TRUE.equals(img.getIsActive()));
            heroRepo.save(img);
        });
    }

    public void moveUp(Long id) {
        List<HeroImage> all = heroRepo.findAllByOrderByDisplayOrderAsc();
        for (int i = 1; i < all.size(); i++) {
            if (all.get(i).getId().equals(id)) {
                swap(all.get(i), all.get(i - 1));
                break;
            }
        }
    }

    public void moveDown(Long id) {
        List<HeroImage> all = heroRepo.findAllByOrderByDisplayOrderAsc();
        for (int i = 0; i < all.size() - 1; i++) {
            if (all.get(i).getId().equals(id)) {
                swap(all.get(i), all.get(i + 1));
                break;
            }
        }
    }

    public void saveConfig(boolean cycleMode, Long pinnedImageId) {
        HeroCarouselConfig cfg = getOrCreateConfig();
        cfg.setCycleMode(cycleMode);
        cfg.setPinnedImageId(cycleMode ? null : pinnedImageId);
        configRepo.save(cfg);
    }

    private void swap(HeroImage a, HeroImage b) {
        int tmp = a.getDisplayOrder();
        a.setDisplayOrder(b.getDisplayOrder());
        b.setDisplayOrder(tmp);
        heroRepo.save(a);
        heroRepo.save(b);
    }
}