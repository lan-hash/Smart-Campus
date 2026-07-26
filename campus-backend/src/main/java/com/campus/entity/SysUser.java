package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String avatar;

    /** 性别 0未知 1男 2女 */
    private Integer gender;

    private String email;

    private String phone;

    /** 所在学校 */
    private String campus;

    /** 学号 */
    private String studentId;

    /** 真实姓名 */
    private String realName;

    /** 个人简介 */
    private String bio;

    /** 角色 0学生 1管理员 */
    private Integer role;

    /** 状态 0正常 1禁用 */
    private Integer status;

    /** 校园认证 0未认证 1已认证 */
    private Integer campusVerified;

    private Integer postCount;

    private Integer followCount;

    private Integer fansCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}