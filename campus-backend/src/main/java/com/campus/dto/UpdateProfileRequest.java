package com.campus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 20, message = "昵称最多20个字符")
    private String nickname;

    /** 性别 0未知 1男 2女 */
    private Integer gender;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String campus;

    @Size(max = 100, message = "个人简介最多100个字符")
    private String bio;
}