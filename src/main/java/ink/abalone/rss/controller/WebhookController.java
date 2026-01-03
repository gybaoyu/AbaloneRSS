package ink.abalone.rss.controller;

import ink.abalone.rss.entity.Post;
import ink.abalone.rss.entity.RSSBlogPost;
import ink.abalone.rss.entity.RawWebhook;
import ink.abalone.rss.service.PostService;
import ink.abalone.rss.service.WxService;
import ink.abalone.rss.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    public WebhookController(JsonUtils jsonUtils, PostService postService, WxService wxService) {
        this.jsonUtils = jsonUtils;
        this.postService = postService;
        this.wxService = wxService;
    }

    @PostMapping("/update")
    public void handleHaloWebhook(@RequestBody RawWebhook webhook) {
        switch (webhook.getEventType()) {
            case "TEST_WEBHOOK" -> logger.info("收到webhook测试请求,当前连接正常");
            case "NEW_POST" ->{
                Post post = jsonUtils.parseJson(webhook.getData().toString(), Post.class);
                logger.info("检测到新文章【{}】,正在抓取RSSBlogPost", post.getTitle());
                RSSBlogPost blogPost = postService.addPost(post);
                logger.info("RSS抓取完毕,正在同步至公众号");
                wxService.createDraftArticle(blogPost);
            }
            case "DELETE_POST" -> {
                Post post = jsonUtils.parseJson(webhook.getData().toString(), Post.class);
                if (post.getIsPermanent()){
                    logger.info("文章【{}】被彻底删除,同时在公众号上将该文章彻底删除", post.getTitle());
                }else {
                    logger.info("文章【{}】被移入回收站,同时在公众号上将该文章移入草稿箱", post.getTitle());
                }
            }
            default -> logger.error("[ERROR] 收到未知请求: {}", webhook);
        }
    }
}