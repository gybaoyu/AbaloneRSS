package ink.abalone.rss.service.client;

import ink.abalone.rss.config.WxProperties;
import ink.abalone.rss.entity.dto.DraftAddRequest;
import ink.abalone.rss.entity.dto.DraftAddResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class WxService {
    private final WebClient webClient;
    private final WxProperties wxProperties;

    public WxService(WxProperties wxProperties, WebClient webClient) {
        this.wxProperties = wxProperties;
        this.webClient = webClient;
    }

    //获取接口调用凭据
    public String getAccessToken() {
        Map<String, Object> body = Map.of(
                "grant_type", "client_credentials",
                "appid", wxProperties.getAppid(),
                "secret", wxProperties.getAppsecret()
        );
        return webClient.post()
                .uri("https://api.weixin.qq.com/cgi-bin/stable_token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    //新增草稿
    public DraftAddResponse addDraft(String accessToken, DraftAddRequest request) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.weixin.qq.com")
                        .path("/cgi-bin/draft/add")
                        .queryParam("access_token", accessToken)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(DraftAddResponse.class)
                .block();
    }

}