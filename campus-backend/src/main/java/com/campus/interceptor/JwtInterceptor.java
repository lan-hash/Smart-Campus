package com.campus.interceptor;

import com.campus.common.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.campus.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    /**
     * GET 请求公开路径正则白名单（仅限 GET 方法免登录）
     * POST/PUT/DELETE 一律需要登录
     */
    private static final Pattern[] PUBLIC_GET_PATTERNS = {
            Pattern.compile("^/forum/posts/\\d+$"),              // 帖子详情
            Pattern.compile("^/forum/posts/\\d+/comments$"),      // 帖子评论列表
            Pattern.compile("^/confession/\\d+/comments$"),       // 表白评论列表
            Pattern.compile("^/secondhand/products/\\d+$"),       // 商品详情
            Pattern.compile("^/course/orders/\\d+$"),             // 代课订单详情
            Pattern.compile("^/course/orders/\\d+/evaluations$"), // 订单评价列表
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // GET 请求检查公开路径白名单（动态路径 /xxx/{id} 类型）
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            String path = request.getRequestURI();
            for (Pattern p : PUBLIC_GET_PATTERNS) {
                if (p.matcher(path).matches()) {
                    // 公开 GET 接口：如果有 token 则解析用户ID，没有也放行
                    String token = request.getHeader("Authorization");
                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                        try {
                            Long userId = jwtUtils.getUserId(token);
                            request.setAttribute("userId", userId);
                        } catch (Exception ignored) {
                        }
                    }
                    return true;
                }
            }
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token == null || !jwtUtils.validateAccessToken(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, "未登录或登录已过期")));
            return false;
        }

        // 将用户信息存入请求属性，供 Controller 使用
        try {
            Long userId = jwtUtils.getUserId(token);
            request.setAttribute("userId", userId);
        } catch (Exception e) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, "Token无效")));
            return false;
        }

        return true;
    }
}