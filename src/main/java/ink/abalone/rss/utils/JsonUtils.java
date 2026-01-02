package ink.abalone.rss.utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JsonUtils {
    private final ObjectMapper objectMapper;
    public JsonUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // JSON字符串转对象
    public <T> T parseJson(String json,Class<T> tClass){
        try {
            return objectMapper.readValue(json, tClass);
        } catch (JsonProcessingException e) {
            System.err.println(
                    "\n[ERROR] json转对象失败,原json: "+json+
                    "\n[ERROR] 目标类型: "+tClass.getName()+
                    "\n[ERROR] 报错信息: "+e.getMessage());
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
