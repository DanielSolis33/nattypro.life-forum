package nattypro.life.forum;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactSubmissionRepository extends JpaRepository<ContactSubmission, Long> {
    List<ContactSubmission> findAllByOrderBySubmittedAtDesc();
    List<ContactSubmission> findByIsReadFalseOrderBySubmittedAtDesc();
}