package ink.abalone.rss.service.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;

@SpringBootTest
public class WxClientTest {
    @Autowired
    private WxClient wxClient;
    @Test
    public void test() {
        System.out.println(wxClient.getAccessToken());
    }

    @Test
    public void imgDownloadTest(){
//        Path result = wxClient.downloadImageToLocal("https://abalone.ink/apis/api.storage.halo.run/v1alpha1/thumbnails/-/via-uri?uri=https%3A%2F%2Fcdn.abalone.ink%2Fblog%2FScreenshot_2025-12-29-12-00-08-24_25d38497d72d75227633ae777c2e0df5.jpg&#38;size=m");
//        System.out.println(result);
    }
}
