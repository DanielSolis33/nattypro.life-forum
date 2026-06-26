package nattypro.life.forum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    
    List<Post> findByCategory(String category);
    long countByCategory(String category);
    Optional<Post> findFirstByCategoryOrderByCreatedAtDesc(String category);

    // Paginated versions
    Page<Post> findByCategory(String category, Pageable pageable);
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Optional<Post> findFirstByCategoryOrderByCreatedAtDesc(String category);
}

