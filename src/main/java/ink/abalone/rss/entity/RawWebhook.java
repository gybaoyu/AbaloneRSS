package ink.abalone.rss.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//Webhook插件发送的json对象
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RawWebhook {
    private String eventType;
    private String eventTypeName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime hookTime;
    private JsonNode data;
}
