package ink.abalone.rss.service;

import ink.abalone.rss.entity.RSSBlogPost;
import ink.abalone.rss.utils.JsonUtils;
import ink.abalone.rss.utils.RSSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private final JsonUtils jsonUtils;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Logger logger = LoggerFactory.getLogger(RedisService.class);

    public RedisService(JsonUtils jsonUtils, RedisTemplate<String, Object> redisTemplate) {
        this.jsonUtils = jsonUtils;
        this.redisTemplate = redisTemplate;
    }

    /**
     * RSS博客guid->RSS博客
     * key: guid (博客/archive开头的访问地址)
     * value: RSSBlogPost
     * @param blogPost RSSBlogPost
     */
    public void savePost(RSSBlogPost blogPost) {
        if (blogPost == null || blogPost.getGuid() == null) return;
        redisTemplate.opsForValue().set(blogPost.getGuid(), blogPost);
    }

    /**
     * 封面URL->微信MediaID(仅处理封面,微信不需要封面的微信图床URL)
     * key: coverURL
     * value: wx_mediaID
     */
    public void saveCoverURL(String coverURL,String wx_mediaID) {
        if (coverURL == null || coverURL.isEmpty()) return;
        redisTemplate.opsForValue().set(coverURL, wx_mediaID);
    }

    /**
     * 将blogPosts全部存入到redis
     * @param blogPosts 读取到的RSS页面的所有post
     */
    public void saveRSSPosts(List<RSSBlogPost> blogPosts){
        logger.info("接收到{}条RSSBlogPost,正在存入Redis并持久化...", blogPosts.size());
        for (RSSBlogPost blogPost : blogPosts) {
            redisTemplate.opsForValue().set(blogPost.getGuid(), jsonUtils.toJson(blogPost));
        }
    }

    /**
     * 图片/音频之类的url->微信图床下的url
     * @param fileURL 原url
     */
    public void saveFileURL(String fileURL,String wxURL) {
        logger.info("将{}存入到redis,值为{}",fileURL,wxURL);
        redisTemplate.opsForValue().set(fileURL, wxURL);
    }

    // 根据 guid 查询 RSSBlogPost
    public RSSBlogPost getPostByGuid(String guid) {
        String json = (String) redisTemplate.opsForValue().get(guid);
        logger.info("查询到GUID为{}在Redis中存储的json: \n{}\n", guid, json);
        return jsonUtils.parseJson(json,RSSBlogPost.class);
    }

    // 删除
    public void deletePost(String guid) {
        if (Boolean.TRUE.equals(redisTemplate.delete(guid))){
            logger.info("删除成功,删除GUID为: {}", guid);
        }else {
            logger.warn("[WARN] 删除失败,期望删除GUID为: {}", guid);
        }
    }
}
