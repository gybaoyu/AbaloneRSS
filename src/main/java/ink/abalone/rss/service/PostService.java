package ink.abalone.rss.service;

import ink.abalone.rss.entity.Post;
import ink.abalone.rss.entity.RSSBlogPost;
import ink.abalone.rss.entity.RawWebhook;
import ink.abalone.rss.utils.RSSUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    private final RSSUtils rssUtils;

    public PostService(RSSUtils rssUtils) {
        this.rssUtils = rssUtils;
    }

    public RSSBlogPost addPost(Post post) {
        RSSBlogPost blogPost;
        System.out.println("[INFO] 收到 NEW_POST 事件,等待 RSS 更新…");
        blogPost = waitForRssPost(post);
        System.out.println("[INFO] 下面是文章的RSSBlogPost信息\n" + blogPost);
        return blogPost;
    }

    public void handlePost(Post post) {
        RSSBlogPost blogPost = parsePostToRSSBlogPost(post);
        System.out.println(blogPost);
    }

    public RSSBlogPost parsePostToRSSBlogPost(Post post) {
        for (RSSBlogPost rssBlogPost : rssUtils.parseRss())
            if (rssBlogPost.getGuid().equals(post.getSlug())) return rssBlogPost;
        return null;
    }

    private RSSBlogPost waitForRssPost(Post post) {
        int maxRetry = 10;          // 最多等 10 次
        long intervalMs = 1000;     // 每次 1 秒
        String expectedTitle = post.getTitle();
        for (int i = 1; i <= maxRetry; i++) {
            try {
                List<RSSBlogPost> items = rssUtils.parseRss();
                System.out.println("[INFO] 期望查询到: " + expectedTitle);
                System.out.println("[INFO] 当前RSS中含有: " + expectedTitle);
                for (RSSBlogPost item : items) {
                    System.out.println("[INFO] " + item.getTitle());
                    if (expectedTitle.equals(item.getTitle())) {
                        System.out.println("[INFO] RSS 已更新,命中第 " + i + " 次");
                        return item;
                    }
                }
                System.out.println("[INFO] 第 " + i + " 次未命中,等待 RSS 更新…");
                Thread.sleep(intervalMs);
            } catch (Exception e) {
                System.err.println("[WARN] 拉取 RSS 失败,重试中：" + e.getMessage());
            }
        }
        throw new RuntimeException("[ERROR] 等待 RSS 更新超时");
    }

}
