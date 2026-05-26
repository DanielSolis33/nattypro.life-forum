package nattypro.life.forum;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChatInvitePinRepository extends JpaRepository<ChatInvitePin, Long> {
    Optional<ChatInvitePin> findByPin(String pin);
    List<ChatInvitePin> findByCreatedByOrderByCreatedAtDesc(String createdBy);
    List<ChatInvitePin> findAllByOrderByCreatedAtDesc();
}