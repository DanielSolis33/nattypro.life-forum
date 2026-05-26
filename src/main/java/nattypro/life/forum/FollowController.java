package nattypro.life.forum;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/follow")
public class FollowController {

    @Autowired private ThreadFollowRepository followRepo;
    @Autowired private PostRepository postRepo;
    @Autowired private UserRepository userRepo;

    @PostMapping("/post/{postId}")
    @ResponseBody
    public ResponseEntity<?> toggleFollow(@PathVariable Long postId, Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Login required"));
        }

        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        Post post = postRepo.findById(postId).orElseThrow();

        Optional<ThreadFollow> existing = followRepo.findByUserAndPost(user, post);

        if (existing.isPresent()) {
            followRepo.delete(existing.get());
            return ResponseEntity.ok(Map.of("following", false));
        } else {
            ThreadFollow follow = new ThreadFollow();
            follow.setUser(user);
            follow.setPost(post);
            followRepo.save(follow);
            return ResponseEntity.ok(Map.of("following", true));
        }
    }
}