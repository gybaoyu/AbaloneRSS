package ink.abalone.rss.config.interceptor;

import ink.abalone.rss.config.HaloProperties;
import ink.abalone.rss.controller.WebhookController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    private static final String HEADER_TOKEN = "token";
    private final HaloProperties properties;
    private final Logger logger = LoggerFactory.getLogger(TokenInterceptor.class);

    public TokenInterceptor(HaloProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String token = request.getHeader(HEADER_TOKEN);

        if (!properties.getToken().equals(token)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"msg\":\"invalid token\"}");
            logger.warn("invalid token:{}\nrequest:{}",token,request);
            return false;
        }

        return true;
    }
}
