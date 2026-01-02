package ink.abalone.rss.service;

import ink.abalone.rss.cache.Cache;
import ink.abalone.rss.entity.RSSBlogPost;
import ink.abalone.rss.entity.dto.DraftAddRequest;
import ink.abalone.rss.entity.dto.DraftAddResponse;
import ink.abalone.rss.entity.dto.MaterialAddResponse;
import ink.abalone.rss.service.client.WxClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class WxService {

    private final WxClient client;
    private final Cache cache;

    public WxService(WxClient client, Cache cache) {
        this.client = client;
        this.cache = cache;
    }

    public void createDraftArticle(RSSBlogPost post) {
        loadCache(post.getCover().getUrl());
        System.out.println("[INFO] 正在构建草稿");
        //构造草稿内容
        DraftAddRequest.Article article = new DraftAddRequest.Article(post.getTitle(),
                post.getCreator(),
                post.getDescription(),
                post.getLink(),
                cache.getBlogToMIDCache().get(post.getCover().getUrl()),
                1);
        DraftAddRequest request = new DraftAddRequest(List.of(article));
        DraftAddResponse response = client.addDraft(request);
        if (response.getMedia_id() != null) {
            System.out.println("[INFO] 草稿构建成功,id为: " + response.getMedia_id());
            cache.getBlogToDIDCache().put(post.getLink(), response.getMedia_id());
        } else {
            System.err.println("[ERROR] 草稿构建失败,response信息: " + response);
        }
    }

    private void loadCache(String blogURL) {
        Map<String, String> blogToMIDCache = cache.getBlogToMIDCache();
        Map<String, String> MIDToWxCache = cache.getMIDToWxCache();

        if (blogToMIDCache.containsKey(blogURL) && MIDToWxCache.containsKey(blogToMIDCache.get(blogURL))) return;

        MaterialAddResponse response = client.uploadImageByUrl(blogURL);
        if (response.getMedia_id() == null) {
            System.err.println("[ERROR] 图片素材上传失败,response信息: " + response);
            return;
        }
        System.out.println("[INFO] 图片素材上传成功,blogURL: " + blogURL + ",mediaID: " + response.getMedia_id() + "wxURL: " + response.getUrl());
        blogToMIDCache.put(blogURL, response.getMedia_id());
        MIDToWxCache.put(response.getMedia_id(), response.getUrl());
    }
}

