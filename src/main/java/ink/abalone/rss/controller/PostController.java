package ink.abalone.rss.controller;

import ink.abalone.rss.entity.Post;
import ink.abalone.rss.service.PostService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PostController {

//    private final PostService postService;
//
//    public PostController(PostService postService) {
//        this.postService = postService;
//    }
//
//    @PostMapping("/post")
//    public void post(@RequestBody Post post) {
//        postService.handlePost(post);
//    }
}
