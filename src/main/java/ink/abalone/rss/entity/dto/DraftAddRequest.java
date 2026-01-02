package ink.abalone.rss.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DraftAddRequest {
    private List<Article> articles;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Article {
        private String title;//标题
        private String author;//作者
        private String content;//图文消息的具体内容
        private String content_source_url;//图文消息的原文地址
        private String thumb_media_id;//图文消息的封面图片素材id
        private Integer need_open_comment;//是否打开评论

    }
}
