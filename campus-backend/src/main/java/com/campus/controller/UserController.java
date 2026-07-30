package com.campus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.config.OssConfig;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.PageResult;
import com.campus.common.result.Result;
import com.campus.dto.LoginRequest;
import com.campus.dto.RegisterRequest;
import com.campus.dto.UpdateProfileRequest;
import com.campus.entity.SysUser;
import com.campus.mapper.UserMapper;
import com.campus.service.UserService;
import com.campus.vo.LoginResponseVO;
import com.campus.vo.UserVO;
import com.aliyun.oss.OSS;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final OSS ossClient;
    private final OssConfig ossConfig;

    @PostMapping("/login")
    public Result<LoginResponseVO> login(@RequestBody @Valid LoginRequest request) {
        LoginResponseVO vo = userService.login(request);
        return Result.success(vo);
    }

    @PostMapping("/register")
    public Result<LoginResponseVO> register(@RequestBody @Valid RegisterRequest request) {
        LoginResponseVO vo = userService.register(request);
        return Result.success(vo);
    }

    @GetMapping("/info")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getUserInfo(userId));
    }

    @GetMapping("/{userId}")
    public Result<UserVO> getUserDetail(@PathVariable Long userId) {
        return Result.success(userService.getUserDetail(userId));
    }

    @GetMapping("/{userId}/stats")
    public Result<UserVO> getUserStats(@PathVariable Long userId) {
        return Result.success(userService.getUserStats(userId));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(HttpServletRequest request,
                                       @RequestBody @Valid UpdateProfileRequest updateRequest) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updateProfile(userId, updateRequest);
        return Result.success();
    }

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(HttpServletRequest request,
                                       @RequestParam("file") MultipartFile file) {
        Long userId = (Long) request.getAttribute("userId");
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        try {
            String originalName = file.getOriginalFilename();
            String ext = FilenameUtils.getExtension(originalName);
            if (ext == null || ext.isEmpty()) {
                ext = "jpg";
            }
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String objectName = "campus/avatar/" + dateDir + "/" + UUID.randomUUID().toString().replace("-", "") + "." + ext;

            try (InputStream inputStream = file.getInputStream()) {
                ossClient.putObject(ossConfig.getBucketName(), objectName, inputStream);
            }

            String avatarUrl = ossConfig.getUrlPrefix() + objectName;
            // 更新数据库中的头像地址
            userService.updateAvatar(userId, avatarUrl);
            return Result.success(avatarUrl);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("头像上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/campus-verify")
    public Result<Void> campusVerify(HttpServletRequest request,
                                     @RequestBody Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        userService.campusVerify(userId, body.get("studentId"), body.get("realName"));
        return Result.success();
    }

    @PostMapping("/follow/{targetId}")
    public Result<Void> followUser(HttpServletRequest request,
                                    @PathVariable Long targetId) {
        Long userId = (Long) request.getAttribute("userId");
        userService.followUser(userId, targetId);
        return Result.success();
    }

    @DeleteMapping("/follow/{targetId}")
    public Result<Void> unfollowUser(HttpServletRequest request,
                                      @PathVariable Long targetId) {
        Long userId = (Long) request.getAttribute("userId");
        userService.unfollowUser(userId, targetId);
        return Result.success();
    }

    @PostMapping("/block/{targetId}")
    public Result<Void> blockUser(HttpServletRequest request,
                                   @PathVariable Long targetId) {
        Long userId = (Long) request.getAttribute("userId");
        SysUser target = userMapper.selectById(targetId);
        if (target == null) {
            throw new BusinessException("用户不存在");
        }
        userMapper.insertBlock(userId, targetId);
        return Result.success();
    }

    @DeleteMapping("/block/{targetId}")
    public Result<Void> unblockUser(HttpServletRequest request,
                                     @PathVariable Long targetId) {
        Long userId = (Long) request.getAttribute("userId");
        userMapper.deleteBlock(userId, targetId);
        return Result.success();
    }

    @GetMapping("/following")
    public Result<PageResult<UserVO>> getFollowingList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        Page<UserVO> result = userService.getFollowingList(userId, page, size);
        return Result.success(PageResult.of(result));
    }

    @GetMapping("/fans")
    public Result<PageResult<UserVO>> getFansList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        Page<UserVO> result = userService.getFansList(userId, page, size);
        return Result.success(PageResult.of(result));
    }

    @PostMapping("/refresh_tiken")
    public Result<LoginResponseVO> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()){
            throw new BusinessException("refreshToken 不能为空");
        }
        return Result.success(userService.refreshToken(refreshToken));
    }
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        userService.logout(userId, token);
        return Result.success();
    }
}