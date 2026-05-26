package nattypro.life.forum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(?1)")
    Optional<User> findByUsername(String username);
    
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(?1)")
    Optional<User> findByEmail(String email);
    
    Optional<User> findByResetToken(String resetToken);
    Optional<User> findByConfirmationToken(String confirmationToken);
}