package ink.abalone.rss.controller;

import ink.abalone.rss.config.HaloProperties;
import ink.abalone.rss.entity.Post;
import ink.abalone.rss.entity.RSSBlogPost;
import ink.abalone.rss.service.PostService;
import ink.abalone.rss.service.WxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post")
public class PostController {
    private final Logger logger = LoggerFactory.getLogger(PostController.class);

    private final HaloProperties properties;
    private final PostService postService;
    private final WxService wxService;

    public PostController(HaloProperties properties, PostService postService, WxService wxService) {
        this.properties = properties;
        this.postService = postService;
        this.wxService = wxService;
    }
    @GetMapping("/addDraft")
    public void post(@RequestParam String token,
                     @RequestParam String title) {
        if (token.equals(properties.getToken())){
            logger.info("收到addDraft请求,Title:【{}】,正在抓取RSSBlogPost", title);
            RSSBlogPost blogPost = postService.addPost(new Post(title));
            logger.info("RSS抓取完毕,正在同步至公众号");
            wxService.createDraftArticle(blogPost);
        }else{
            logger.warn("/addDraft token错误{}",token);
        }
    }
}
