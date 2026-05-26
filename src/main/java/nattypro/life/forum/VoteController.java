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
	@RequestMapping("/vote")
	public class VoteController {

	    @Autowired private PostVoteRepository postVoteRepo;
	    @Autowired private PostRepository postRepo;
	    @Autowired private UserRepository userRepo;
	    @Autowired private CommentVoteRepository commentVoteRepo;
	    @Autowired private CommentRepository commentRepo;

	    @PostMapping("/post/{postId}")
	    @ResponseBody
	    public ResponseEntity<?> toggleVote(@PathVariable Long postId, Principal principal) {

	        if (principal == null) {
	            return ResponseEntity.status(401).body(Map.of("error", "Login required"));
	        }

	        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
	        Post post = postRepo.findById(postId).orElseThrow();

	        if (post.getAuthor().equals(user.getUsername())) {
	            return ResponseEntity.badRequest().body(Map.of("error", "Can't upvote your own post"));
	        
	        }
	        Optional<PostVote> existing = postVoteRepo.findByUserAndPost(user, post);

	        
	        if (existing.isPresent()) {
	            postVoteRepo.delete(existing.get());  // toggle OFF
	        } else {
	            PostVote vote = new PostVote();
	            vote.setUser(user);
	            vote.setPost(post);
	            postVoteRepo.save(vote);              // toggle ON
	        }

	        long newCount = postVoteRepo.countByPost(post);
	        boolean voted = postVoteRepo.findByUserAndPost(user, post).isPresent();

	        return ResponseEntity.ok(Map.of("voteCount", newCount, "voted", voted));
	    }
	    @PostMapping("/comment/{commentId}")
	    @ResponseBody
	    public ResponseEntity<?> toggleCommentVote(@PathVariable Long commentId, Principal principal) {
	        if (principal == null) {
	            return ResponseEntity.status(401).body(Map.of("error", "Login required"));
	        }
	        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
	        Comment comment = commentRepo.findById(commentId).orElseThrow();

	        if (comment.getAuthor().equals(user.getUsername())) {
	            return ResponseEntity.badRequest().body(Map.of("error", "Can't upvote your own comment"));
	        }

	        Optional<CommentVote> existing = commentVoteRepo.findByUserAndComment(user, comment);
	        if (existing.isPresent()) {
	            commentVoteRepo.delete(existing.get());
	        } else {
	            CommentVote vote = new CommentVote();
	            vote.setUser(user);
	            vote.setComment(comment);
	            commentVoteRepo.save(vote);
	        }

	        long newCount = commentVoteRepo.countByComment(comment);
	        boolean voted = commentVoteRepo.findByUserAndComment(user, comment).isPresent();
	        return ResponseEntity.ok(Map.of("voteCount", newCount, "voted", voted));
	    }
	}


