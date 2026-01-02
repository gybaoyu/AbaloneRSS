package ink.abalone.rss.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import ink.abalone.rss.config.WxProperties;
import ink.abalone.rss.entity.dto.AccessTokenResponse;
import ink.abalone.rss.entity.dto.DraftAddRequest;
import ink.abalone.rss.entity.dto.DraftAddResponse;
import ink.abalone.rss.entity.dto.MaterialAddResponse;
import ink.abalone.rss.utils.JsonUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
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

@Service
public class WxClient {

    private final JsonUtils jsonUtils;
    private final WebClient webClient;
    private final WxProperties wxProperties;

    public WxClient(JsonUtils jsonUtils, WxProperties wxProperties, WebClient webClient) {
        this.jsonUtils = jsonUtils;
        this.wxProperties = wxProperties;
        this.webClient = webClient;
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

        System.out.println("【微信原始返回】" + raw);

        if (raw == null || raw.isBlank()) {
            throw new RuntimeException("微信 draft/add 返回空响应");
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(raw, DraftAddResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("解析 draft/add 返回失败，原始内容: " + raw, e);
        }
    }


    public MaterialAddResponse uploadImageByUrl(String imageUrl) {
        byte[] imageBytes = webClient.get()
                .uri(imageUrl)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
        if (imageBytes == null || imageBytes.length == 0) throw new RuntimeException("图片下载失败：" + imageUrl);
        System.out.println("[INFO] Blog图片下载成功");
        Path imagePath;
        try {
            imagePath = Paths.get("/home/ubuntu/rss/resource/" + System.currentTimeMillis() + ".jpg");
            Files.write(imagePath, imageBytes);
        } catch (IOException e) {
            throw new RuntimeException("图片写入失败", e);
        }
        String url = "https://api.weixin.qq.com/cgi-bin/material/add_material"
                + "?access_token=" + getAccessToken()
                + "&type=image";
        List<String> command = List.of(
                "curl",
                "-s",
                "-X", "POST",
                url,
                "-F", "media=@" + imagePath.toAbsolutePath()
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
            System.out.println("【curl exitCode】" + process.waitFor());
            System.out.println("【微信原始返回】" + response);

            if (process.waitFor() != 0 || response.isBlank()) throw new RuntimeException("curl 执行失败或微信返回空");
            return jsonUtils.parseJson(response, MaterialAddResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("curl 上传微信素材失败", e);
        }
    }

}
