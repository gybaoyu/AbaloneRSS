package ink.abalone.rss.controller;

import ink.abalone.rss.entity.Post;
import ink.abalone.rss.entity.RSSBlogPost;
import ink.abalone.rss.entity.RawWebhook;
import ink.abalone.rss.service.PostService;
import ink.abalone.rss.service.WxService;
import ink.abalone.rss.utils.JsonUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/webhook")
public class WebhookController {
    private final JsonUtils jsonUtils;
    private final PostService postService;
    private final WxService wxService;
    public WebhookController(JsonUtils jsonUtils, PostService postService, WxService wxService) {
        this.jsonUtils = jsonUtils;
        this.postService = postService;
        this.wxService = wxService;
    }

    @PostMapping("/update")
    public void handleHaloWebhook(@RequestBody RawWebhook webhook) {
        switch (webhook.getEventType()) {
            case "TEST_WEBHOOK" -> System.out.println("[INFO] 收到webhook测试请求,当前连接正常");
            case "NEW_POST" ->{
                Post post = jsonUtils.parseJson(webhook.getData().toString(), Post.class);
                System.out.println("[INFO] 检测到新文章【"+post.getTitle()+"】,正在抓取RSSBlogPost");
                RSSBlogPost blogPost = postService.addPost(post);
                System.out.println("[INFO] RSS抓取完毕,正在同步至公众号");
                wxService.createDraftArticle(blogPost);
            }
            case "DELETE_POST" -> {
                Post post = jsonUtils.parseJson(webhook.getData().toString(), Post.class);
                if (post.getIsPermanent()){
                    System.out.println("[INFO] 文章【"+post.getTitle()+"】被彻底删除,同时在公众号上将该文章彻底删除");
                }else {
                    System.out.println("[INFO] 文章【"+post.getTitle()+"】被移入回收站,同时在公众号上将该文章移入草稿箱");
                }
            }
            default -> System.err.println("[ERROR] 收到未知请求: "+webhook);
        }
    }
}