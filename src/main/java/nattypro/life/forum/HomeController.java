	package nattypro.life.forum;
	
	import java.time.LocalDateTime;
	import java.util.Arrays;
	import java.util.List;
	import java.util.Map;
	import java.util.stream.Collectors;
	
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.security.core.Authentication;
	import org.springframework.stereotype.Controller;
	import org.springframework.ui.Model;
	import org.springframework.web.bind.annotation.GetMapping;
	import org.springframework.web.bind.annotation.PathVariable;
	import org.springframework.web.bind.annotation.PostMapping;
	import org.springframework.web.bind.annotation.RequestParam;
	import org.springframework.data.domain.Page;
	import org.springframework.data.domain.PageRequest;
	import org.springframework.data.domain.Sort;
	import org.springframework.web.multipart.MultipartFile;
	import java.util.ArrayList;
	import org.jsoup.Jsoup;
	import org.jsoup.safety.Safelist;
	
	@Controller
	public class HomeController {
	    
	    @Autowired
	    private PostRepository postRepository;
	    @Autowired
	    private CommentRepository commentRepository;
	    @Autowired
	    private UserRepository userRepository;
	    @Autowired
	    private ThreadFollowRepository followRepo;
	    @Autowired
	    private PostVoteRepository postVoteRepo;
	    @Autowired
	    private AnnouncementRepository announcementRepo;
	    @Autowired
	    private BannerRepository bannerRepo;
	    @Autowired
	    private YouTubeFeedService youtubeFeedService;
	    @Autowired
	    private CommentVoteRepository commentVoteRepo;
	    @Autowired
	    private S3Service s3Service;
		@Autowired
		private HeroImageService heroImageService;
	    
	 // Define all categories in one place
	    private static final List<String> CATEGORIES = Arrays.asList(
	        "Training",
	        "Nutrition",
	        "Recovery", 
	        "Mindset",
	        "Prep Files",
	        "Coaches Corner",
	        "Self Promotion",
	        "Random Side Quests"
	        
	    );
	    
	    @GetMapping("/")
	    public String home(@RequestParam(required = false) String category,
	                       @RequestParam(defaultValue = "0") int page,
	                       Model model, Authentication authentication) {

	        int pageSize = 10;
	        PageRequest pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
	        
			
List<Post> posts;

        if (category != null && !category.isEmpty()) {
            Page<Post> postPage = postRepository.findByCategory(category, pageable);
            posts = postPage.getContent();
            model.addAttribute("selectedCategory", category);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", postPage.getTotalPages());
        } else {
            // One most-recent post per category for the home view
            posts = CATEGORIES.stream()
                .map(cat -> postRepository.findFirstByCategoryOrderByCreatedAtDesc(cat))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toList());
            model.addAttribute("selectedCategory", "All");
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
        }

        model.addAttribute("selectedCategoryParam", category != null ? category : "");

        // Build excerptMap — strip HTML tags, truncate to ~33%
        Map<Long, String> excerptMap = posts.stream().collect(Collectors.toMap(
            Post::getId,
            p -> {
                String text = Jsoup.parse(p.getContent()).text();
                int limit = Math.max(80, text.length() / 3);
                return text.length() > limit ? text.substring(0, limit) + "…" : text;
            }
        ));
        model.addAttribute("excerptMap", excerptMap);

        // Build proStatusMap
        Map<String, Object> proStatusMap = posts.stream()
	    	    .collect(Collectors.toMap(
	    	        Post::getAuthor,
	    	        p -> {
	    	            User u = userRepository.findByUsername(p.getAuthor()).orElse(null);
	    	            return u != null && u.getVerifiedNattyPro() != null && u.getVerifiedNattyPro();
	    	        },
	    	        (a, b) -> a
	    	    ));
	    	model.addAttribute("proStatusMap", proStatusMap);
	
	    	model.addAttribute("posts", posts);
	        model.addAttribute("newPost", new Post());
	        
	       
	     // Add category counts for sidebar
	        model.addAttribute("trainingCount", postRepository.countByCategory("Training"));
	        model.addAttribute("nutritionCount", postRepository.countByCategory("Nutrition"));
	        model.addAttribute("recoveryCount", postRepository.countByCategory("Recovery"));
	        model.addAttribute("mindsetCount", postRepository.countByCategory("Mindset"));
	        model.addAttribute("prepFilesCount", postRepository.countByCategory("Prep Files"));
	        model.addAttribute("coachesCornerCount", postRepository.countByCategory("Coaches Corner"));
	        model.addAttribute("selfPromotionCount", postRepository.countByCategory("Self Promotion"));
	        model.addAttribute("randomSideQuestsCount", postRepository.countByCategory("Random Side Quests"));
	        model.addAttribute("totalCount", postRepository.count());
	        model.addAttribute("categories", CATEGORIES);
	        
	     // User stats and rank for header

		// Banner + YouTube — available to guests and members alike
model.addAttribute("announcements", announcementRepo.findByIsActiveTrueOrderByCreatedAtDesc());
model.addAttribute("banners", bannerRepo.findByIsActiveTrueOrderByDisplayOrderAsc());
try {
    if (youtubeFeedService.hasCachedVideos()) {
        model.addAttribute("youtubeVideos", youtubeFeedService.getLatestVideos());
    } else {
        youtubeFeedService.initializeCache();
        model.addAttribute("youtubeVideos", youtubeFeedService.getLatestVideos());
    }
} catch (Exception e) {
    System.err.println("YouTube feed unavailable: " + e.getMessage());
}

	        if (authentication != null) {
	            User currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
	            model.addAttribute("currentUser", currentUser);
	            
	            // Count user's posts
	            long userPostCount = postRepository.findAll().stream()
	                .filter(p -> p.getAuthor().equals(authentication.getName()))
	                .count();
	            
	            // Determine rank based on post count
	            String userRank;
	            String rankEmoji;
	            if (userPostCount == 0) {
	                userRank = "Lurker";
	                rankEmoji = "👀";
	            } else if (userPostCount <= 2) {
	                userRank = "Newb";
	                rankEmoji = "🌱";
	            } else if (userPostCount <= 7) {
	                userRank = "Member";
	                rankEmoji = "💪";
	            } else if (userPostCount <= 14) {
	                userRank = "Regular";
	                rankEmoji = "⭐";
	            } else if (userPostCount <= 29) {
	                userRank = "Veteran";
	                rankEmoji = "🔥";
	            } else {
	                userRank = "Legend";
	                rankEmoji = "👑";
	            }
	            
	            model.addAttribute("userPostCount", userPostCount);
	            model.addAttribute("userRank", userRank);
	            model.addAttribute("rankEmoji", rankEmoji);
	            model.addAttribute("heroSlides", heroImageService.getActiveSlides());
	        }
	        
	        return "home";
	    }
	    
	    @PostMapping("/create")
	    public String createPost(Post post,
	                             @RequestParam(required = false) List<MultipartFile> images,
	                             Authentication authentication) {
	        post.setAuthor(authentication.getName());

	        if (images != null) {
	            List<String> imageUrls = new ArrayList<>();
	            int count = 0;
	            for (MultipartFile image : images) {
	                if (count >= 5) break;
	                if (image != null && !image.isEmpty()) {
	                    String contentType = image.getContentType();
	                    if (contentType != null && (contentType.equals("image/jpeg") ||
	                            contentType.equals("image/png") ||
	                            contentType.equals("image/gif"))) {
	                        if (image.getSize() <= 5 * 1024 * 1024) {
	                            try {
	                                String url = s3Service.uploadFile(image);
	                                imageUrls.add(url);
	                                count++;
	                            } catch (Exception e) {
	                                System.err.println("Image upload failed: " + e.getMessage());
	                            }
	                        }
	                    }
	                }
	            }
	            post.setImageUrls(imageUrls);
	        }

	        Safelist safelist = Safelist.relaxed()
	        	    .addTags("iframe")
	        	    .addAttributes("iframe", "src", "width", "height", "frameborder",
	        	                   "allowfullscreen", "allow", "style")
	        	    .addProtocols("iframe", "src", "https");

	        	post.setContent(Jsoup.clean(post.getContent(), safelist));
	        	postRepository.save(post);
	        
	        return "redirect:/";
	    }
	    
	    @PostMapping("/delete/{id}")
	    public String deletePost(@PathVariable Long id, Authentication authentication) {
	        Post post = postRepository.findById(id).orElse(null);
	        
	        if (post != null) {
	            String currentUser = authentication.getName();
	            boolean isAdmin = authentication.getAuthorities().stream()
	                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
	            
	            if (post.getAuthor().equals(currentUser) || isAdmin) {
	                // Delete S3 images
	                if (post.getImageUrls() != null) {
	                    for (String url : post.getImageUrls()) {
	                        try {
	                            s3Service.deleteFile(url);
	                        } catch (Exception e) {
	                            System.err.println("Failed to delete image: " + e.getMessage());
	                        }
	                    }
	                }
	                followRepo.deleteByPost(post);
	                postRepository.delete(post);
	            }
	        }
	        
	        return "redirect:/";
	    }
	    
	    @GetMapping("/post/{id}/edit")
	    public String editPostForm(@PathVariable Long id, Model model, Authentication authentication) {
	        Post post = postRepository.findById(id).orElse(null);
	        if (post == null) return "redirect:/";

	        // Only author or admin can edit
	        boolean isAdmin = authentication.getAuthorities().stream()
	            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
	        if (!post.getAuthor().equals(authentication.getName()) && !isAdmin) {
	            return "redirect:/";
	        }

	        model.addAttribute("post", post);
	        model.addAttribute("categories", CATEGORIES);
	        return "edit-post";
	    }

	    @PostMapping("/post/{id}/edit")
	    public String editPostSubmit(@PathVariable Long id,
	                                 @RequestParam String title,
	                                 @RequestParam String content,
	                                 @RequestParam String category,
	                                 Authentication authentication) {

	        System.out.println("EDIT SUBMIT - content received: " + content); // ← add this
	        
	        

	        Post post = postRepository.findById(id).orElse(null);
	        
	        if (post == null) return "redirect:/";

	        boolean isAdmin = authentication.getAuthorities().stream()
	            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
	        if (!post.getAuthor().equals(authentication.getName()) && !isAdmin) {
	            return "redirect:/";
	        }

	        post.setTitle(title);
	        post.setCategory(category);

	        Safelist safelist = Safelist.relaxed()
	            .addTags("iframe")
	            .addAttributes("iframe", "src", "width", "height", "frameborder",
	                           "allowfullscreen", "allow", "style")
	            .addProtocols("iframe", "src", "https");

	        post.setContent(Jsoup.clean(content, safelist));
	        System.out.println("AFTER JSOUP - content length: " + post.getContent().length());

	        postRepository.save(post);

	        return "redirect:/post/" + id;
	    }
	
	    @GetMapping("/post/{id}")
	    public String viewPost(@PathVariable Long id, Model model, Authentication authentication) {
	        Post post = postRepository.findById(id).orElse(null);
	        if (post == null) {
	            return "redirect:/";
	        }

	        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(id);
	        model.addAttribute("post", post);
	        model.addAttribute("comments", comments);
	        model.addAttribute("newComment", new Comment());

	        User postAuthor = userRepository.findByUsername(post.getAuthor()).orElse(null);
	        boolean postAuthorPro = postAuthor != null && Boolean.TRUE.equals(postAuthor.getVerifiedNattyPro());
	        model.addAttribute("postAuthorPro", postAuthorPro);

	        Map<String, Object> commentProStatus = comments.stream()
	            .collect(Collectors.toMap(
	                Comment::getAuthor,
	                c -> {
	                    User u = userRepository.findByUsername(c.getAuthor()).orElse(null);
	                    return u != null && Boolean.TRUE.equals(u.getVerifiedNattyPro());
	                },
	                (a, b) -> a
	            ));
	        model.addAttribute("commentProStatus", commentProStatus);

	        // Guest-safe defaults — set unconditionally so the template never hits a null.
	        model.addAttribute("isFollowing", false);
	        model.addAttribute("hasVoted", false);
	        model.addAttribute("commentVotedMap", Map.of());

	        User currentUser = (authentication != null)
	            ? userRepository.findByUsername(authentication.getName()).orElse(null)
	            : null;
	        model.addAttribute("currentUser", currentUser);

	        // currentUser is null for a guest (no "anonymousUser" row) — this block is the real gate.
	        if (currentUser != null) {
	            model.addAttribute("isFollowing", followRepo.existsByUserAndPost(currentUser, post));
	            model.addAttribute("hasVoted", postVoteRepo.findByUserAndPost(currentUser, post).isPresent());
	            Map<Long, Boolean> commentVotedMap = comments.stream()
	                .collect(Collectors.toMap(
	                    Comment::getId,
	                    c -> commentVoteRepo.findByUserAndComment(currentUser, c).isPresent()
	                ));
	            model.addAttribute("commentVotedMap", commentVotedMap);
	        }

	        return "post";
	    }
	    
	    @GetMapping("/search")
	    public String search(@RequestParam String query, Model model, Authentication authentication) {
	        
	    	List<Post> results = postRepository.findAll().stream()
	    		    .filter(p -> 
	    		        p.getTitle().toLowerCase().contains(query.toLowerCase()) ||
	    		        p.getContent().toLowerCase().contains(query.toLowerCase()) ||
	    		        (p.getAuthor().toLowerCase().contains(query.toLowerCase()) 
	    		            && !p.getAuthor().startsWith("DeletedUser_"))
	    		    )
	    		    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
	    		    .collect(Collectors.toList());
	        Map<String, Object> proStatusMap = results.stream()
	        	    .collect(Collectors.toMap(
	        	        Post::getAuthor,
	        	        p -> {
	        	            User u = userRepository.findByUsername(p.getAuthor()).orElse(null);
	        	            return u != null && u.getVerifiedNattyPro() != null && u.getVerifiedNattyPro();
	        	        },
	        	        (a, b) -> a
	        	    ));
	   
	        
	        model.addAttribute("results", results);
	        model.addAttribute("query", query);
	        model.addAttribute("proStatusMap", proStatusMap);
	        model.addAttribute("resultCount", results.size());
	
	        // User stats for header
	        if (authentication != null) {
	            User currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
	            model.addAttribute("currentUser", currentUser);
	            long userPostCount = postRepository.findAll().stream()
	                .filter(p -> p.getAuthor().equals(authentication.getName()))
	                .count();
	            String userRank;
	            String rankEmoji;
	            if (userPostCount == 0) { userRank = "Lurker"; rankEmoji = "👀"; }
	            else if (userPostCount <= 2) { userRank = "Newb"; rankEmoji = "🌱"; }
	            else if (userPostCount <= 7) { userRank = "Member"; rankEmoji = "💪"; }
	            else if (userPostCount <= 14) { userRank = "Regular"; rankEmoji = "⭐"; }
	            else if (userPostCount <= 29) { userRank = "Veteran"; rankEmoji = "🔥"; }
	            else { userRank = "Legend"; rankEmoji = "👑"; }
	            model.addAttribute("userPostCount", userPostCount);
	            model.addAttribute("userRank", userRank);
	            model.addAttribute("rankEmoji", rankEmoji);
	        }
	
	        return "search-results";
	    }
	
	    @PostMapping("/post/{id}/comment")
	    public String addComment(@PathVariable Long id, @RequestParam String content, Authentication authentication) {
	        Comment comment = new Comment();
	        comment.setContent(content);
	        comment.setPostId(id);
	        comment.setAuthor(authentication.getName());
	        comment.setCreatedAt(LocalDateTime.now());  // ← Add this line
	        commentRepository.save(comment);
	        return "redirect:/post/" + id;
	    }
}