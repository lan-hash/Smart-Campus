package com.campus.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer gender;
    private String email;
    private String phone;
    private String campus;
    private String studentId;
    private String realName;
    private String bio;
    /** 角色 0学生 1管理员 */
    private Integer role;
    /** 校园认证状态 */
    private Integer campusVerified;
    private Integer postCount;
    private Integer followCount;
    private Integer fansCount;
    private LocalDateTime createTime;

    /** 仅登录时返回 Token，其他接口不返回 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String token;
}