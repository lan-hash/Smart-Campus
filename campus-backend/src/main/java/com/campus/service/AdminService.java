package com.campus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.dto.HandleReportRequest;
import com.campus.dto.LoginRequest;
import com.campus.dto.PublishNoticeRequest;
import com.campus.entity.SystemNotice;
import com.campus.vo.DashboardVO;
import com.campus.vo.ForumPostVO;
import com.campus.vo.ReportVO;
import com.campus.vo.UserVO;

public interface AdminService {

    /**
     * 管理员登录
     */
    UserVO login(LoginRequest request);

    /**
     * 获取数据面板统计
     */
    DashboardVO getDashboard();

    /**
     * 获取用户列表（分页+搜索）
     */
    Page<UserVO> getUserList(int page, int size, String keyword);

    /**
     * 修改用户状态（封禁/解封）
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 获取举报列表（分页）
     */
    Page<ReportVO> getReportList(int page, int size, Integer status);

    /**
     * 处理举报（通过或驳回）
     */
    void handleReport(Long reportId, Long adminId, HandleReportRequest request);

    /**
     * 获取帖子列表（分页）
     */
    Page<ForumPostVO> getPostList(int page, int size, String keyword);

    /**
     * 修改帖子状态（正常/违规）
     */
    void updatePostStatus(Long postId, Integer status);

    /**
     * 帖子置顶/取消置顶
     */
    void updatePostTop(Long postId, Integer isTop);

    /**
     * 帖子加精/取消加精
     */
    void updatePostEssence(Long postId, Integer isEssence);

    /**
     * 获取公告列表
     */
    Page<SystemNotice> getNoticeList(int page, int size);

    /**
     * 发布公告
     */
    void publishNotice(Long adminId, PublishNoticeRequest request);

    /**
     * 删除公告
     */
    void deleteNotice(Long noticeId);
}
