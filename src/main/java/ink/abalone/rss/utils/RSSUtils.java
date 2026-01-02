package ink.abalone.rss.utils;

import com.rometools.rome.feed.module.DCModule;
import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import ink.abalone.rss.entity.Enclosure;
import ink.abalone.rss.entity.RSSBlogPost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class RSSUtils {

    private final URI uri;

    public RSSUtils(@Value("${rss.uri}") String rawUri) {
        try {
            this.uri = URI.create(rawUri);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid RSS URI", e);
        }
    }

    public List<RSSBlogPost> parseRss() {
        try {
            SyndFeed feed = new SyndFeedInput().build(
                    new XmlReader(fetchRssStream())
            );
            List<RSSBlogPost> result = new ArrayList<>();
            for (SyndEntry entry : feed.getEntries())
                result.add(convert(entry));

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse RSS via Rome", e);
        }
    }

    private RSSBlogPost convert(SyndEntry entry) {
        return new RSSBlogPost(
                entry.getTitle(),
                entry.getLink(),
                getDescription(entry),
                entry.getUri(),                 // guid
                getCreator(entry),              // dc:creator
                getCategories(entry),
                toLocalDateTime(entry.getPublishedDate()),
                getCover(entry)                 // enclosure
        );
    }

    /* ================== helpers ================== */

    private String getDescription(SyndEntry entry) {
        SyndContent desc = entry.getDescription();
        return desc != null ? desc.getValue() : "";
    }

    private String getCreator(SyndEntry entry) {
        // dc:creator
        DCModule dc = (DCModule) entry.getModule(DCModule.URI);
        if (dc != null && dc.getCreator() != null) {
            return dc.getCreator();
        }
        return entry.getAuthor(); // fallback
    }

    private List<String> getCategories(SyndEntry entry) {
        List<String> categories = new ArrayList<>();
        for (SyndCategory c : entry.getCategories()) {
            categories.add(c.getName());
        }
        return categories;
    }

    private Enclosure getCover(SyndEntry entry) {
        if (entry.getEnclosures() == null || entry.getEnclosures().isEmpty()) {
            return null;
        }
        SyndEnclosure e = entry.getEnclosures().getFirst();
        return new Enclosure(
                e.getUrl(),
                e.getType(),
                e.getLength() > 0 ? String.valueOf(e.getLength()) : null
        );
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return LocalDateTime.ofInstant(
                date.toInstant(),
                ZoneId.of("Asia/Shanghai")
        );
    }

    private java.io.InputStream fetchRssStream() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        return client
                .send(request, HttpResponse.BodyHandlers.ofInputStream())
                .body();
    }
}
