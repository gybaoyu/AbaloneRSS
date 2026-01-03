package ink.abalone.rss.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "halo")
@Data
public class HaloProperties {
    private String uri;
    private String blogAttachmentHost;//博客原附件储存域名
    /**
     * 由于逻辑为先下载博客原附件中的图片,再上传至微信,所以需要一个中转路径保存附件,一般为服务器的地址
     */
    private String filePath;//附件保存路径
}
