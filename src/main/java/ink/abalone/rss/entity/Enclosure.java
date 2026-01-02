package ink.abalone.rss.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Enclosure {
    private String url;
    private String type;
    private String length;
}
