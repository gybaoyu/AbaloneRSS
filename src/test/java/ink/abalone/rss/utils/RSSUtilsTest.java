package ink.abalone.rss.utils;

import ink.abalone.rss.entity.RSSBlogPost;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

@SpringBootTest
public class RSSUtilsTest {
    @Autowired
    private RSSUtils rssUtils;
    @Test
    public void parseRssTest(){
        for (RSSBlogPost rssBlogPost : rssUtils.parseRss()) {
            System.out.println(rssBlogPost);
        }
    }
}
