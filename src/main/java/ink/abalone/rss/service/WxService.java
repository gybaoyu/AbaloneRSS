package ink.abalone.rss.service;

import ink.abalone.rss.config.HaloProperties;
import ink.abalone.rss.entity.RSSBlogPost;
import ink.abalone.rss.entity.dto.DraftAddRequest;
import ink.abalone.rss.entity.dto.DraftAddResponse;
import ink.abalone.rss.entity.dto.MaterialAddResponse;
import ink.abalone.rss.service.client.WxClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WxService {
    private final RedisService redisService;
    private final WxClient client;
    private final HaloProperties properties;
    private final Logger logger = LoggerFactory.getLogger(WxService.class);

    private final Pattern iamgePattern = Pattern.compile("<img\\b[^>]*\\bsrc\\s*=\\s*(['\"])(.*?)\\1[^>]*>", Pattern.CASE_INSENSITIVE);
    private final Pattern audioPattern = Pattern.compile("<audio\\b[^>]*\\bsrc\\s*=\\s*(['\"])(.*?)\\1[^>]*>", Pattern.CASE_INSENSITIVE);


    public WxService(RedisService redisService, WxClient client, HaloProperties properties) {
        this.redisService = redisService;
        this.client = client;
        this.properties = properties;
    }

    public void createDraftArticle(RSSBlogPost post) {
//        loadCache(post.getCover().getUrl());//cover的全链接
        logger.info("正在构建草稿");
        String coverID = uploadArticleCover(post);
        //构造草稿内容

        String content = processContentURLS(post.getDescription(), iamgePattern);
//        content = processContentURLS(content,audioPattern);
        DraftAddRequest.Article article = new DraftAddRequest.Article(
                post.getTitle(),
                post.getCreator(),
                content,
                properties.getUri() + post.getGuid(),//原文连接
                coverID,
                1);
        DraftAddRequest request = new DraftAddRequest(List.of(article));
        DraftAddResponse response = client.addDraft(request);
        if (response.getMedia_id() != null) {
            logger.info("草稿构建成功,id为: {}", response.getMedia_id());
//            cache.getBlogToDIDCache().put(post.getGuid(), response.getMedia_id());
        } else {
            logger.error("[ERROR] 草稿构建失败,response信息: {}", response);
        }
    }

    private String uploadArticleCover(RSSBlogPost post) {
        if (post.getCover() != null)
            logger.info("正在上传【{}】的封面: {}\n", post.getTitle(), post.getCover().getUrl());
        else {
            logger.error("【{}】没有封面, 同步失败\n", post.getTitle());
            throw new RuntimeException("博客文章没有封面图片,无法上传至公众号");
        }
        MaterialAddResponse response = client.uploadFileByUrl(post.getCover().getUrl(), "image");
        logger.info("上传成功: {}\n", response);
        logger.info("正在储存至Redis: \nkey={}\nvalue={}\n", post.getCover().getUrl(), response.getMedia_id());
        redisService.saveCoverURL(post.getCover().getUrl(), response.getMedia_id());
        return response.getMedia_id();
    }

    /**
     * 处理文章中原本存在的图片或音频链接,转为微信提供的图床链接
     *
     * @param rawContent RSSBlogPost的文章内容
     * @return 处理后的RSSBlogPost的文章内容
     */
    public String processContentURLS(String rawContent, Pattern pattern) {
        Matcher matcher = pattern.matcher(rawContent);
        StringBuilder sb = new StringBuilder();
        logger.info("当前匹配表达式: {}", pattern);
        logger.info("开始替换路径");
        while (matcher.find()) {
            String originalTag = matcher.group(0); // 整个<img>(<audio>)标签

            String quote = matcher.group(1);       // 引号类型
            String url = matcher.group(2);         // src 的 URL
            String replacementTag;

            if (url.contains(properties.getBlogAttachmentHost())) {
                logger.info("正在替换: {}", originalTag);
                MaterialAddResponse response = client.uploadFileByUrl(url, "image");
                String newUrl = response.getUrl();
                redisService.saveFileURL(url, newUrl);
                replacementTag = originalTag.replace(url, newUrl);
                logger.info("已替换为: {}", replacementTag);
            } else {
                //plugins/feed/assets/telemetry.gif halo提供的rss访问计数,略过
                continue;
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacementTag));
        }
        matcher.appendTail(sb);
        logger.info("替换完成,替换结果: {}", sb);
        return sb.toString();

    }
}

