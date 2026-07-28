package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.exception.BusinessException;
import com.campus.common.util.JwtUtils;
import com.campus.dto.HandleReportRequest;
import com.campus.dto.LoginRequest;
import com.campus.dto.PublishNoticeRequest;
import com.campus.entity.ForumPost;
import com.campus.entity.OperationLog;
import com.campus.entity.Report;
import com.campus.entity.SysUser;
import com.campus.entity.SystemNotice;
import com.campus.mapper.*;
import com.campus.service.AdminService;
import com.campus.vo.DashboardVO;
import com.campus.vo.ForumPostVO;
import com.campus.vo.ReportVO;
import com.campus.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final ForumPostMapper forumPostMapper;
    private final ReportMapper reportMapper;
    private final OperationLogMapper operationLogMapper;
    private final SystemNoticeMapper systemNoticeMapper;
    private final ConfessionMapper confessionMapper;
    private final SecondhandProductMapper secondhandProductMapper;
    private final CourseOrderMapper courseOrderMapper;
    private final JwtUtils jwtUtils;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserVO login(LoginRequest request) {
        SysUser user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getRole() != 1) {
            throw new BusinessException("该账号不是管理员");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
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
    public DashboardVO getDashboard() {
        DashboardVO vo = new DashboardVO();
        vo.setTotalUsers(userMapper.selectCount(null));
        vo.setTodayNewUsers(userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .apply("DATE(create_time) = CURDATE()")));
        vo.setTotalPosts(forumPostMapper.selectCount(null));
        vo.setTotalProducts(secondhandProductMapper.selectCount(null));
        vo.setTotalCourseOrders(courseOrderMapper.selectCount(null));
        vo.setTotalConfessions(confessionMapper.selectCount(null));
        vo.setPendingReports(reportMapper.countPendingReports());
        return vo;
    }

    @Override
    public Page<UserVO> getUserList(int page, int size, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getNickname, keyword)
                    .or().like(SysUser::getPhone, keyword);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> userPage = userMapper.selectPage(new Page<>(page, size), wrapper);

        Page<UserVO> voPage = new Page<>(page, size, userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getRole() == 1) {
            throw new BusinessException("不能修改管理员状态");
        }
        userMapper.update(null,
                new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getId, userId)
                        .set(SysUser::getStatus, status));
    }

    @Override
    public Page<ReportVO> getReportList(int page, int size, Integer status) {
        return reportMapper.selectReportPage(new Page<>(page, size), status);
    }

    @Override
    public void handleReport(Long reportId, Long adminId, HandleReportRequest request) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("举报记录不存在");
        }
        if (report.getStatus() != 0) {
            throw new BusinessException("该举报已被处理");
        }
        if (request.getStatus() != 1 && request.getStatus() != 2) {
            throw new BusinessException("状态值无效，只能为1(处理)或2(驳回)");
        }

        report.setStatus(request.getStatus());
        report.setHandlerId(adminId);
        report.setHandleRemark(request.getHandleRemark());
        report.setHandleTime(LocalDateTime.now());
        reportMapper.updateById(report);
    }

    @Override
    public Page<ForumPostVO> getPostList(int page, int size, String keyword) {
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(ForumPost::getTitle, keyword)
                    .or().like(ForumPost::getContent, keyword);
        }
        wrapper.orderByDesc(ForumPost::getCreateTime);
        Page<ForumPost> postPage = forumPostMapper.selectPage(new Page<>(page, size), wrapper);

        Page<ForumPostVO> voPage = new Page<>(page, size, postPage.getTotal());
        voPage.setRecords(postPage.getRecords().stream().map(this::toPostVO).toList());
        return voPage;
    }

    @Override
    public void updatePostStatus(Long postId, Integer status) {
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        forumPostMapper.update(null,
                new LambdaUpdateWrapper<ForumPost>()
                        .eq(ForumPost::getId, postId)
                        .set(ForumPost::getStatus, status));
    }

    @Override
    public void updatePostTop(Long postId, Integer isTop) {
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        forumPostMapper.update(null,
                new LambdaUpdateWrapper<ForumPost>()
                        .eq(ForumPost::getId, postId)
                        .set(ForumPost::getIsTop, isTop));
    }

    @Override
    public void updatePostEssence(Long postId, Integer isEssence) {
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        forumPostMapper.update(null,
                new LambdaUpdateWrapper<ForumPost>()
                        .eq(ForumPost::getId, postId)
                        .set(ForumPost::getIsEssence, isEssence));
    }

    @Override
    public Page<SystemNotice> getNoticeList(int page, int size) {
        LambdaQueryWrapper<SystemNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SystemNotice::getCreateTime);
        return systemNoticeMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public void publishNotice(Long adminId, PublishNoticeRequest request) {
        SystemNotice notice = new SystemNotice();
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setAdminId(adminId);
        systemNoticeMapper.insert(notice);
    }

    @Override
    public void deleteNotice(Long noticeId) {
        systemNoticeMapper.deleteById(noticeId);
    }

    // ========== 私有工具方法 ==========

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

    private ForumPostVO toPostVO(ForumPost post) {
        ForumPostVO vo = new ForumPostVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setCategoryId(post.getCategoryId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setImages(post.getImages());
        vo.setViewCount(post.getViewCount());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setCollectCount(post.getCollectCount());
        vo.setIsTop(post.getIsTop());
        vo.setIsEssence(post.getIsEssence());
        vo.setStatus(post.getStatus());
        vo.setAiCategory(post.getAiCategory());
        vo.setCreateTime(post.getCreateTime());

        // 查询作者信息
        SysUser author = userMapper.selectById(post.getUserId());
        if (author != null) {
            vo.setAuthorId(author.getId());
            vo.setAuthorNickname(author.getNickname());
            vo.setAuthorAvatar(author.getAvatar());
        }
        return vo;
    }
}
