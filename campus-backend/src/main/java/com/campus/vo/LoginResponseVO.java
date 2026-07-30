package com.campus.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseVO {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;          // accessToken 过期时间（秒）
    private UserVO user;

    public static LoginResponseVO of(String accessToken, String refreshToken,
                                     Long expiresIn, UserVO user) {
        LoginResponseVO vo = new LoginResponseVO();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setExpiresIn(expiresIn);
        vo.setUser(user);
        return vo;
    }
}