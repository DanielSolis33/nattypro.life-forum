package nattypro.life.forum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.List;

public interface PostVoteRepository extends JpaRepository<PostVote, Long> {

    Optional<PostVote> findByUserAndPost(User user, Post post);

    long countByPost(Post post);

    long countByPostIn(List<Post> posts); // for bulk reputation calc
}