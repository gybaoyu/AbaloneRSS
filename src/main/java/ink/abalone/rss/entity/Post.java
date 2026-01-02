package ink.abalone.rss.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

//Webhook文章
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Post {
    private String title;//文章标题
    private String slug;//文章别名(用于拼接生成链接)
    private String permalink;//文章链接
    private String visible;//公开(PUBLIC) 私有(PRIVATE)
    private String owner;//作者 (abalone)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;//文章创建时间
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;//文章发布时间
    /**
     * null - 没有进行删除相关操作
     * true - 放入回收站并进行永久删除
     * false - 放入回收站
     */
    private Boolean isPermanent;
    /**
     * null - 没有进行删除相关操作
     * true - 从回收站取出
     * false - 保持放在回收站
     */
    private Boolean isRestore;
}
