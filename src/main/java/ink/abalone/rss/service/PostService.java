package ink.abalone.rss.service;

import ink.abalone.rss.entity.Post;
import ink.abalone.rss.entity.RSSBlogPost;
import ink.abalone.rss.entity.RawWebhook;
import ink.abalone.rss.utils.RSSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    private final RSSUtils rssUtils;
    private final RedisService redisService;
    private final Logger logger = LoggerFactory.getLogger(PostService.class);

    public PostService(RSSUtils rssUtils, RedisService redisService) {
        this.rssUtils = rssUtils;
        this.redisService = redisService;
    }

    public RSSBlogPost addPost(Post post) {
        RSSBlogPost blogPost;
        logger.info("收到 NEW_POST 事件,等待 RSS 更新…");
        blogPost = waitForRssPost(post);
        logger.info("下面是文章的RSSBlogPost信息\n{}", blogPost);
        return blogPost;
    }

//    public void handlePost(Post post) {
//        RSSBlogPost blogPost = parsePostToRSSBlogPost(post);
//        System.out.println(blogPost);
//    }
//
//    public RSSBlogPost parsePostToRSSBlogPost(Post post) {
//        for (RSSBlogPost rssBlogPost : rssUtils.parseRss())
//            if (rssBlogPost.getGuid().equals(post.getSlug())) return rssBlogPost;
//        return null;
//    }

    private RSSBlogPost waitForRssPost(Post post) {
        int maxRetry = 10;          // 最多等 10 次
        long intervalMs = 1000;     // 每次 1 秒
        String expectedTitle = post.getTitle();
        for (int i = 1; i <= maxRetry; i++) {
            try {
                List<RSSBlogPost> items = rssUtils.parseRss();
                logger.info("期望查询到: {}", expectedTitle);
                for (RSSBlogPost item : items) {
                    if (expectedTitle.equals(item.getTitle())) {
                        logger.info("RSS 已更新,命中第 {} 次", i);
                        logger.info("正在将RSS内容存入Redis并持久化...");
                        redisService.saveRSSPosts(items);
                        return item;
                    }
                }
                logger.info("第 {} 次未命中,等待 RSS 更新…", i);
                Thread.sleep(intervalMs);
            } catch (Exception e) {
                logger.warn("[WARN] 拉取 RSS 失败,重试中：{}", e.getMessage());
            }
        }
        throw new RuntimeException("[ERROR] 等待 RSS 更新超时");
    }

}
