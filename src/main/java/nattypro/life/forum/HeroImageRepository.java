package nattypro.life.forum;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HeroImageRepository extends JpaRepository<HeroImage, Long> {
    List<HeroImage> findByIsActiveTrueOrderByDisplayOrderAsc();
    List<HeroImage> findAllByOrderByDisplayOrderAsc();
}