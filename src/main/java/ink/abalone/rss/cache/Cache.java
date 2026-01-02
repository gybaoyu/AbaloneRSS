package ink.abalone.rss.cache;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Getter
public class Cache {
    // 博客素材url -> mediaId
    private final Map<String, String> blogToMIDCache = new ConcurrentHashMap<>();
    // mediaId -> 微信素材url
    private final Map<String, String> MIDToWxCache = new ConcurrentHashMap<>();

    // 博客url -> 草稿MID
    private final Map<String, String> blogToDIDCache = new ConcurrentHashMap<>();
}

