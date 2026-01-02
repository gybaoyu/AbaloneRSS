package ink.abalone.rss.cache;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Getter
public class Cache {
    // 博客素材url -> mediaId
    private final Map<String, String> blogToMediaIDCache = new ConcurrentHashMap<>();
    // mediaId -> 微信素材url
    private final Map<String, String> mediaIDToWxCache = new ConcurrentHashMap<>();
}

