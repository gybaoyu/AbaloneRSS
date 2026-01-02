package ink.abalone.rss.service;

import ink.abalone.rss.service.client.WxClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class WxClientTest {
    @Autowired
    private WxClient wxClient;
    @Test
    public void test() {
        System.out.println(wxClient.getAccessToken());
    }
}
