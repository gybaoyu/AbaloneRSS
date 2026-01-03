package ink.abalone.rss.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import ink.abalone.rss.config.HaloProperties;
import ink.abalone.rss.config.WxProperties;
import ink.abalone.rss.entity.dto.AccessTokenResponse;
import ink.abalone.rss.entity.dto.DraftAddRequest;
import ink.abalone.rss.entity.dto.DraftAddResponse;
import ink.abalone.rss.entity.dto.MaterialAddResponse;
import ink.abalone.rss.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.netty.util.AsciiString.containsIgnoreCase;

@Service
public class WxClient {

    private final JsonUtils jsonUtils;
    private final WebClient webClient;
    private final WxProperties wxProperties;
    private final HaloProperties haloProperties;
    private final Logger logger = LoggerFactory.getLogger(WxClient.class);

    public WxClient(JsonUtils jsonUtils, WxProperties wxProperties, WebClient webClient, HaloProperties haloProperties) {
        this.jsonUtils = jsonUtils;
        this.wxProperties = wxProperties;
        this.webClient = webClient;
        this.haloProperties = haloProperties;
    }

    // 获取接口调用凭据
    public String getAccessToken() {
        Map<String, Object> body = Map.of(
                "grant_type", "client_credential",
                "appid", wxProperties.getAppid(),
                "secret", wxProperties.getAppsecret()
        );

        AccessTokenResponse response = webClient.post()
                .uri("https://api.weixin.qq.com/cgi-bin/stable_token")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(AccessTokenResponse.class)
                .block();

        if (response != null) {
            return response.getAccess_token();
        } else {
            throw new RuntimeException("获取getAccessToken失败");
        }
    }

    // 新增草稿
    public DraftAddResponse addDraft(DraftAddRequest request) {
        String raw = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.weixin.qq.com")
                        .path("/cgi-bin/draft/add")
                        .queryParam("access_token", getAccessToken())
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL) // ⭐ 关键：接受 text/plain
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (raw == null || raw.isBlank()) {
            throw new RuntimeException("微信 draft/add 返回空响应");
        }else logger.info("微信原始返回: {}", raw);
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(raw, DraftAddResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("解析 draft/add 返回失败，原始内容: " + raw, e);
        }
    }


    /**
     * 上传图片素材
     * @param imageUrl 图片全链接
     * @param type 文件类型:image 图片, audio
     * @return 微信素材id和微信图床下的imgURL
     */
    public MaterialAddResponse uploadFileByUrl(String imageUrl,String type) {
        // 调用新的方法下载图片并返回本地路径
        Path filePath = downloadFileToLocal(imageUrl);

        String url = "https://api.weixin.qq.com/cgi-bin/material/add_material"
                + "?access_token=" + getAccessToken()
                + "&type="+type;
        List<String> command = List.of(
                "curl",
                "-s",
                "-X", "POST",
                url,
                "-F", "media=@" + filePath.toAbsolutePath()
        );

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            String response;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                response = reader.lines().collect(Collectors.joining("\n"));
            }
            logger.info("curl exitCode: {}", process.waitFor());
            logger.info("微信原始返回: {}", response);

            if (process.waitFor() != 0 || response.isBlank())
                throw new RuntimeException("[ERROR] curl 执行失败或微信返回空");

            return jsonUtils.parseJson(response, MaterialAddResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("[ERROR] curl 上传微信素材失败", e);
        }
    }

    /**
     * 下载图片到本地并返回保存的 Path
     */
    public Path downloadFileToLocal(String fileUrl) {
        System.out.println(fileUrl);
        while (fileUrl.contains("%25")) fileUrl = fileUrl.replace("%25", "%");
        System.out.println(fileUrl);
        fileUrl = fileUrl.replace("&#38;", "&");
        byte[] imageBytes = webClient.get()
                .uri(fileUrl)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

        if (imageBytes == null || imageBytes.length == 0)
            throw new RuntimeException("File下载失败：" + fileUrl);

        logger.info("Blog File下载成功");

        try {
            Path filePath = Paths.get(haloProperties.getFilePath() + System.currentTimeMillis() + getFileExtension(fileUrl));
            Files.write(filePath, imageBytes);
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException("File写入失败", e);
        }
    }



    private String getFileExtension(String url) {
        if (containsIgnoreCase(url,".bmp")){
            return ".bmp";
        }else if (containsIgnoreCase(url,".gif")){
            return ".gif";
        }else if (containsIgnoreCase(url,".jpg")){
            return ".jpg";
        }else if (containsIgnoreCase(url,".png")){
            return ".png";
        }else if (containsIgnoreCase(url,".jpeg")){
            return ".jpeg";
        }else if (containsIgnoreCase(url,".mp3")){
            return ".mp3";
        }else if (containsIgnoreCase(url,".mp4")){
            return ".mp4";
        }else if (containsIgnoreCase(url,".wav")){
            return ".wav";
        }else if (containsIgnoreCase(url,".amr")){
            return ".amr";
        }else if (containsIgnoreCase(url,".wma")){
            return ".wma";
        }else {
            logger.error("上传的文件类型不支持{}",url);
            throw new RuntimeException("上传的图片类型不支持");
        }
    }
}


