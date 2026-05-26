package nattypro.life.forum;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // Find all comments for a specific post
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
    
    // Count comments for a post
    long countByPostId(Long postId);
}