package nattypro.life.forum;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "thread_follows",
uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"}))
public class ThreadFollow {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@ManyToOne
@JoinColumn(name = "user_id", nullable = false)
private User user;

@ManyToOne
@JoinColumn(name = "post_id", nullable = false)
private Post post;

@Column(name = "followed_at")
private LocalDateTime followedAt = LocalDateTime.now();

public Long getId() { return id; }
public User getUser() { return user; }
public void setUser(User user) { this.user = user; }
public Post getPost() { return post; }
public void setPost(Post post) { this.post = post; }
public LocalDateTime getFollowedAt() { return followedAt; }
}
