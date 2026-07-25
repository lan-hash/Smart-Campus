package com.campus.common.config;

import com.campus.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 拦截器注册：需要登录才能访问的路径
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                // 拦截所有 /api 下的请求（前端通过 vite proxy 把 /api 代理到后端）
                .addPathPatterns("/**")
                // 排除不需要登录的接口
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/user/{id}",
                        "/user/{id}/stats",
                        "/forum/categories",
                        "/forum/posts",
                        "/forum/posts/{id}",
                        "/forum/posts/{id}/comments",
                        "/confession/list",
                        "/confession/{id}/comments",
                        "/secondhand/categories",
                        "/secondhand/products",
                        "/secondhand/products/{id}",
                        "/course/orders",
                        "/course/orders/{id}",
                        "/message/notices",
                        "/upload/**",
                        "/admin/login",
                        // 静态资源和 Swagger
                        "/favicon.ico",
                        "/error"
                );
    }

}