package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.exception.BusinessException;
import com.campus.common.util.JwtUtils;
import com.campus.dto.LoginRequest;
import com.campus.dto.RegisterRequest;
import com.campus.dto.UpdateProfileRequest;
import com.campus.entity.SysUser;
import com.campus.mapper.UserMapper;
import com.campus.service.UserService;
import com.campus.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;

    @Value("${upload.path:./uploads/}")
    private String uploadPath;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserVO login(LoginRequest request) {
        SysUser user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 1) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());
        String token = jwtUtils.generateToken(claims);

        UserVO vo = toVO(user);
        vo.setToken(token);
        return vo;
    }

    @Override
    public void register(RegisterRequest request) {
        SysUser existing = userMapper.selectByUsername(request.getUsername());
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setCampus(request.getCampus());
        user.setGender(0);
        user.setRole(0);
        user.setStatus(0);
        user.setCampusVerified(0);
        user.setPostCount(0);
        user.setFollowCount(0);
        user.setFansCount(0);
        user.setAvatar("/default-avatar.svg");
        userMapper.insert(user);
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toVO(user);
    }

    @Override
    public UserVO getUserDetail(Long userId) {
        return getUserInfo(userId);
    }

    @Override
    public UserVO getUserStats(Long userId) {
        return getUserInfo(userId);
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUser::getId, userId);
        if (request.getNickname() != null) {
            wrapper.set(SysUser::getNickname, request.getNickname());
        }
        if (request.getGender() != null) {
            wrapper.set(SysUser::getGender, request.getGender());
        }
        if (request.getEmail() != null) {
            wrapper.set(SysUser::getEmail, request.getEmail());
        }
        if (request.getCampus() != null) {
            wrapper.set(SysUser::getCampus, request.getCampus());
        }
        if (request.getBio() != null) {
            wrapper.set(SysUser::getBio, request.getBio());
        }
        userMapper.update(null, wrapper);
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUser::getId, userId).set(SysUser::getAvatar, avatarUrl);
        userMapper.update(null, wrapper);
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
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
            String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            File dir = new File(uploadPath + dateDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file.transferTo(new File(dir, fileName));
            String avatarUrl = "/uploads/" + dateDir + "/" + fileName;
            LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(SysUser::getId, userId).set(SysUser::getAvatar, avatarUrl);
            userMapper.update(null, wrapper);
            return avatarUrl;
        } catch (IOException e) {
            throw new BusinessException("头像上传失败");
        }
    }

    @Override
    @Transactional
    public void campusVerify(Long userId, String studentId, String realName) {
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUser::getId, userId)
                .set(SysUser::getStudentId, studentId)
                .set(SysUser::getRealName, realName)
                .set(SysUser::getCampusVerified, 1);
        userMapper.update(null, wrapper);
    }

    @Override
    @Transactional
    public void followUser(Long userId, Long targetId) {
        if (userId.equals(targetId)) {
            throw new BusinessException("不能关注自己");
        }
        SysUser target = userMapper.selectById(targetId);
        if (target == null) {
            throw new BusinessException("用户不存在");
        }
        if (userMapper.isFollowing(userId, targetId) > 0) {
            throw new BusinessException("已经关注了该用户");
        }
        userMapper.insertFollow(userId, targetId);
        userMapper.update(null,
                new LambdaUpdateWrapper<SysUser>().eq(SysUser::getId, userId).setSql("follow_count = follow_count + 1"));
        userMapper.update(null,
                new LambdaUpdateWrapper<SysUser>().eq(SysUser::getId, targetId).setSql("fans_count = fans_count + 1"));
    }

    @Override
    @Transactional
    public void unfollowUser(Long userId, Long targetId) {
        int rows = userMapper.deleteFollow(userId, targetId);
        if (rows > 0) {
            userMapper.update(null,
                    new LambdaUpdateWrapper<SysUser>().eq(SysUser::getId, userId).setSql("follow_count = follow_count - 1"));
            userMapper.update(null,
                    new LambdaUpdateWrapper<SysUser>().eq(SysUser::getId, targetId).setSql("fans_count = fans_count - 1"));
        }
    }

    @Override
    public Page<UserVO> getFollowingList(Long userId, int page, int size) {
        List<UserVO> list = userMapper.selectFollowingList(userId);
        int total = list.size();
        int from = (page - 1) * size;
        int to = Math.min(from + size, total);
        if (from >= total) {
            list = Collections.emptyList();
        } else {
            list = list.subList(from, to);
        }
        Page<UserVO> result = new Page<>(page, size);
        result.setRecords(list);
        result.setTotal(total);
        return result;
    }

    @Override
    public Page<UserVO> getFansList(Long userId, int page, int size) {
        List<UserVO> list = userMapper.selectFansList(userId);
        int total = list.size();
        int from = (page - 1) * size;
        int to = Math.min(from + size, total);
        if (from >= total) {
            list = Collections.emptyList();
        } else {
            list = list.subList(from, to);
        }
        Page<UserVO> result = new Page<>(page, size);
        result.setRecords(list);
        result.setTotal(total);
        return result;
    }

    private UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setGender(user.getGender());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setCampus(user.getCampus());
        vo.setStudentId(user.getStudentId());
        vo.setRealName(user.getRealName());
        vo.setBio(user.getBio());
        vo.setRole(user.getRole());
        vo.setCampusVerified(user.getCampusVerified());
        vo.setPostCount(user.getPostCount());
        vo.setFollowCount(user.getFollowCount());
        vo.setFansCount(user.getFansCount());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}