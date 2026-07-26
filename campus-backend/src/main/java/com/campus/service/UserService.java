package com.campus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.dto.LoginRequest;
import com.campus.dto.RegisterRequest;
import com.campus.dto.UpdateProfileRequest;
import com.campus.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    UserVO login(LoginRequest request);

    void register(RegisterRequest request);

    UserVO getUserInfo(Long userId);

    UserVO getUserDetail(Long userId);

    UserVO getUserStats(Long userId);

    void updateProfile(Long userId, UpdateProfileRequest request);

    String uploadAvatar(Long userId, MultipartFile file);

    void updateAvatar(Long userId, String avatarUrl);

    void campusVerify(Long userId, String studentId, String realName);

    void followUser(Long userId, Long targetId);

    void unfollowUser(Long userId, Long targetId);

    Page<UserVO> getFollowingList(Long userId, int page, int size);

    Page<UserVO> getFansList(Long userId, int page, int size);
}