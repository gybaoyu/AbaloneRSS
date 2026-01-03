package ink.abalone.rss.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class WxServiceTest {
    @Autowired
    WxService wxService;
    @Test
    public void processContentURLSTest() {
        String content="";
//        wxService.processContentURLS(content);
    }
}
