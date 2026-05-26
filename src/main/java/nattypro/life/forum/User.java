package nattypro.life.forum;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 20)
    @Size(min = 3, max = 20, message = "Username must be 3-20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(unique = true, nullable = false, length = 100)
    @Email(message = "Must be a valid email")
    private String email;
    
    @Column(nullable = false, length = 20)
    private String role = "USER";
    
    @Column(name = "verified_natty_pro")
    private Boolean verifiedNattyPro = false;

    public Boolean getVerifiedNattyPro() { return verifiedNattyPro; }
    public void setVerifiedNattyPro(Boolean verifiedNattyPro) { this.verifiedNattyPro = verifiedNattyPro; }
    
    // ========== PROFILE FIELDS ==========
    
    @Column(name = "avatar_url", length = 255)
    private String avatarUrl = "/images/default-avatar.png";
    
    @Column(length = 500)
    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;
    
    @Min(value = 13, message = "Must be at least 13 years old")
    @Max(value = 120, message = "Age seems invalid")
    private Integer age;
    
    @Column(name = "instagram_handle", length = 30)
    @Size(max = 30, message = "Instagram handle too long")
    private String instagramHandle;
    
    @Column(name = "twitter_handle", length = 30)
    @Size(max = 30, message = "Twitter handle too long")
    private String twitterHandle;
    
    @Column(name = "youtube_handle", length = 50)
    @Size(max = 50, message = "YouTube handle too long")
    private String youtubeHandle;
    
    @Column(name = "twitch_handle", length = 30)
    @Size(max = 30, message = "Twitch handle too long")
    private String twitchHandle;

    public String getTwitchHandle() { return twitchHandle; }
    public void setTwitchHandle(String twitchHandle) { this.twitchHandle = twitchHandle; }
    
    @Column(name = "rep_points")
    private Integer repPoints = 0;
    
    @Column(name = "show_email")
    private Boolean showEmail = false;
    
    @Column(name = "show_age")
    private Boolean showAge = true;
    
    @Column(name = "accepted_rules")
    private Boolean acceptedRules = false;

    @Column(name = "chat_access")
    private Boolean chatAccess = false;

    @Column(name = "chat_muted_until")
    private LocalDateTime chatMutedUntil;

    @Column(name = "chat_suspended")
    private Boolean chatSuspended = false;
    
    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    @Column(name = "email_confirmed")
    private Boolean emailConfirmed = false;

    @Column(name = "confirmation_token")
    private String confirmationToken;
    
    // ========== CONSTRUCTORS ==========
    
    public User() { 
        this.role = "USER"; 
        this.repPoints = 0;
        this.avatarUrl = "/images/default-avatar.png";
        this.showEmail = false;
        this.showAge = true;
    }
    
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = "USER";
        this.repPoints = 0;
        this.avatarUrl = "/images/default-avatar.png";
        this.showEmail = false;
        this.showAge = true;
    }
    
    // ========== GETTERS AND SETTERS ==========
    
    public Boolean getAcceptedRules() { return acceptedRules; }
    public void setAcceptedRules(Boolean acceptedRules) { this.acceptedRules = acceptedRules; }

    public Boolean getChatAccess() { return chatAccess; }
    public void setChatAccess(Boolean chatAccess) { this.chatAccess = chatAccess; }

    public LocalDateTime getChatMutedUntil() { return chatMutedUntil; }
    public void setChatMutedUntil(LocalDateTime chatMutedUntil) { this.chatMutedUntil = chatMutedUntil; }

    public Boolean getChatSuspended() { return chatSuspended; }
    public void setChatSuspended(Boolean chatSuspended) { this.chatSuspended = chatSuspended; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }

    public Boolean getEmailConfirmed() { return emailConfirmed; }
    public void setEmailConfirmed(Boolean emailConfirmed) { this.emailConfirmed = emailConfirmed; }

    public String getConfirmationToken() { return confirmationToken; }
    public void setConfirmationToken(String confirmationToken) { this.confirmationToken = confirmationToken; }
    
    // Profile field getters/setters
    
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    
    public String getInstagramHandle() { return instagramHandle; }
    public void setInstagramHandle(String instagramHandle) { this.instagramHandle = instagramHandle; }
    
    public String getTwitterHandle() { return twitterHandle; }
    public void setTwitterHandle(String twitterHandle) { this.twitterHandle = twitterHandle; }
    
    public String getYoutubeHandle() { return youtubeHandle; }
    public void setYoutubeHandle(String youtubeHandle) { this.youtubeHandle = youtubeHandle; }
    
    public Integer getRepPoints() { return repPoints; }
    public void setRepPoints(Integer repPoints) { this.repPoints = repPoints; }
    
    public Boolean getShowEmail() { return showEmail; }
    public void setShowEmail(Boolean showEmail) { this.showEmail = showEmail; }
    
    public Boolean getShowAge() { return showAge; }
    public void setShowAge(Boolean showAge) { this.showAge = showAge; }
}
