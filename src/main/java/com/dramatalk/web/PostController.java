package com.dramatalk.web;

import com.dramatalk.domain.drama.Drama;
import com.dramatalk.domain.drama.DramaRepository;
import com.dramatalk.domain.post.Comment;
import com.dramatalk.domain.post.CommentRepository;
import com.dramatalk.domain.post.Post;
import com.dramatalk.domain.post.PostRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class PostController {

    private final DramaRepository dramaRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public PostController(DramaRepository dramaRepository, PostRepository postRepository, CommentRepository commentRepository) {
        this.dramaRepository = dramaRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @GetMapping("/dramas/{dramaId}/posts/new")
    public String newPost(@PathVariable Long dramaId, Model model) {
        Drama drama = dramaRepository.findById(dramaId).orElseThrow();
        model.addAttribute("drama", drama);
        model.addAttribute("postForm", new PostForm());
        return "posts/new";
    }

    @PostMapping("/dramas/{dramaId}/posts")
    public String createPost(@PathVariable Long dramaId,
                             @Valid @ModelAttribute PostForm postForm,
                             BindingResult bindingResult,
                             Model model) {
        Drama drama = dramaRepository.findById(dramaId).orElseThrow();

        if (bindingResult.hasErrors()) {
            model.addAttribute("drama", drama);
            return "posts/new";
        }

        Post post = new Post(drama, postForm.getTitle(), postForm.getContent());
        postRepository.save(post);
        return "redirect:/posts/" + post.getId();
    }

    @GetMapping("/posts/{postId}")
    public String postDetail(@PathVariable Long postId, Model model) {
        Post post = postRepository.findById(postId).orElseThrow();
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);

        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("commentForm", new CommentForm());
        return "posts/detail";
    }

    @PostMapping("/posts/{postId}/comments")
    public String addComment(@PathVariable Long postId,
                             @Valid @ModelAttribute CommentForm commentForm,
                             BindingResult bindingResult,
                             Model model) {
        Post post = postRepository.findById(postId).orElseThrow();

        if (bindingResult.hasErrors()) {
            List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
            model.addAttribute("post", post);
            model.addAttribute("comments", comments);
            return "posts/detail";
        }

        commentRepository.save(new Comment(post, commentForm.getContent()));
        return "redirect:/posts/" + postId;
    }
}
