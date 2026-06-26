package nattypro.life.forum;

import jakarta.persistence.*;

@Entity
@Table(name = "hero_carousel_config")
public class HeroCarouselConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // true = rotate through all active images; false = show pinnedImageId only
    @Column(name = "cycle_mode")
    private Boolean cycleMode = true;

    @Column(name = "pinned_image_id")
    private Long pinnedImageId;

    public Long getId() { return id; }
    public Boolean getCycleMode() { return cycleMode; }
    public void setCycleMode(Boolean cycleMode) { this.cycleMode = cycleMode; }
    public Long getPinnedImageId() { return pinnedImageId; }
    public void setPinnedImageId(Long pinnedImageId) { this.pinnedImageId = pinnedImageId; }
}