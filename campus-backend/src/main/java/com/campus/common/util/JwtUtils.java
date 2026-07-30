package com.campus.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String REFRESH_PREFIX = "jwt:refresh:";

    public JwtUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 accessToken
     */
    public String generateAccessToken(Map<String, Object> claims) {
        return buildToken(claims, accessExpiration);
    }

    /**
     * 生成 refreshToken，并存入 Redis（key=userId，value=token）
     * 同一用户只保留一个有效 refreshToken
     */
    public String generateRefreshToken(Map<String, Object> claims) {
        claims.put("type","refresh");
        String token = buildToken(claims, refreshExpiration);
        Long userId = Long.valueOf(claims.get("userId").toString());
        redisTemplate.opsForValue().set(REFRESH_PREFIX + userId, token);
        return token;
    }

    /**
     * 构建 Token
     * @param claims
     * @param accessExpiration
     * @return
     */
    private String buildToken(Map<String, Object> claims, long accessExpiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(getKey())
                .compact();
    }
    /**
     * 解析 Token，返回 Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 Token 中获取用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.get("userId").toString());
    }

    /**
     * 判断accessToken是否有效 + 不在黑名单中
     */
    public boolean validateAccessToken(String token) {
        try {
            parseToken(token);
            return !isBlacklisted(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (!"refresh".equals(claims.get("type"))){
                return false;
            }
            Long userId = Long.valueOf(claims.get("userId").toString());
            String stored = (String) redisTemplate.opsForValue().get(REFRESH_PREFIX + userId);
            return token.equals(stored);
        }catch (Exception e){
            return false;
        }
    }

    public void blacklistToken(String token) {
        try {
            Claims claims = parseToken(token);
            long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (remaining > 0) {
                redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "1", remaining, TimeUnit.MILLISECONDS);
            }
        } catch (Exception ignored) {
        }
    }
    public void logoutClear(String accessToken){
        try {
            Claims claims = parseToken(accessToken);
            Long userId = Long.valueOf(claims.get("userId").toString());
            //拉黑当前accessToken
            blacklistToken(accessToken);
            //删除用户所有有效refreshToken，实现立即下线
            redisTemplate.delete(REFRESH_PREFIX + userId);
        }catch (Exception e){
            log.error("退出登录清理token异常",e);
        }
    }
    private boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }

    public long getAccessExpiration(){
        return accessExpiration;
    }
}
