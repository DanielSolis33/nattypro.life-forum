package nattypro.life.forum;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {
    Optional<CommentVote> findByUserAndComment(User user, Comment comment);
    long countByComment(Comment comment);
}