package ink.abalone.rss.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Data
@AllArgsConstructor
public class RSSBlogPost {
    private String title;
    private String link;//全链接
    private String description;
    private String guid;// /archive开头的链接
    private String creator;
    private List<String> categories;
    private LocalDateTime pubDate;
    /**
     * 博客封面
     * example(raw):
     * <enclosure url="https://example.com/podcast.mp3"
     * type="image/jpeg"
     * length="1024000" />
     */
    private Enclosure cover;

}

