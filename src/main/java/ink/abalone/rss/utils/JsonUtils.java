package ink.abalone.rss.utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JsonUtils {
    private final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
    private final ObjectMapper objectMapper;
    public JsonUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // JSON字符串转对象
    public <T> T parseJson(String json,Class<T> tClass){
        try {
            return objectMapper.readValue(json, tClass);
        } catch (JsonProcessingException e) {
            logger.error("\n[ERROR] json转对象失败,原json: {}\n[ERROR] 目标类型: {}\n[ERROR] 报错信息: {}", json, tClass.getName(), e.getMessage());
            throw new RuntimeException(e);
        }
    }
    // 对象转JSON字符串
    public String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
