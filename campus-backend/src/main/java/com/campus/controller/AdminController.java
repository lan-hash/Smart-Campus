package com.campus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.result.PageResult;
import com.campus.common.result.Result;
import com.campus.dto.HandleReportRequest;
import com.campus.dto.LoginRequest;
import com.campus.dto.PublishNoticeRequest;
import com.campus.entity.OperationLog;
import com.campus.entity.SystemNotice;
import com.campus.mapper.OperationLogMapper;
import com.campus.service.AdminService;
import com.campus.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final OperationLogMapper operationLogMapper;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<LoginResponseVO> login(@RequestBody @Valid LoginRequest request) {
        LoginResponseVO vo = adminService.login(request);
        return Result.success(vo);
    }

    /**
     * 数据面板统计
     */
    @GetMapping("/dashboard")
    public Result<DashboardVO> getDashboard() {
        return Result.success(adminService.getDashboard());
    }

    /**
     * 用户列表（分页+搜索）
     */
    @GetMapping("/users")
    public Result<PageResult<UserVO>> getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Page<UserVO> result = adminService.getUserList(page, size, keyword);
        return Result.success(PageResult.of(result));
    }

    /**
     * 封禁/解封用户
     */
    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(HttpServletRequest request,
                                          @PathVariable Long id,
                                          @RequestBody java.util.Map<String, Integer> body) {
        Long adminId = (Long) request.getAttribute("userId");
        Integer status = body.get("status");
        adminService.updateUserStatus(id, status);
        saveLog(adminId, "修改用户状态", "PUT", "/admin/users/" + id + "/status", request);
        return Result.success();
    }

    /**
     * 举报列表
     */
    @GetMapping("/reports")
    public Result<PageResult<ReportVO>> getReportList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Page<ReportVO> result = adminService.getReportList(page, size, status);
        return Result.success(PageResult.of(result));
    }

    /**
     * 处理举报
     */
    @PutMapping("/reports/{id}/handle")
    public Result<Void> handleReport(HttpServletRequest request,
                                      @PathVariable Long id,
                                      @RequestBody @Valid HandleReportRequest handleRequest) {
        Long adminId = (Long) request.getAttribute("userId");
        adminService.handleReport(id, adminId, handleRequest);
        saveLog(adminId, "处理举报", "PUT", "/admin/reports/" + id + "/handle", request);
        return Result.success();
    }

    /**
     * 帖子列表
     */
    @GetMapping("/posts")
    public Result<PageResult<ForumPostVO>> getPostList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Page<ForumPostVO> result = adminService.getPostList(page, size, keyword);
        return Result.success(PageResult.of(result));
    }

    /**
     * 帖子审核（修改状态：正常/违规）
     */
    @PutMapping("/posts/{id}/status")
    public Result<Void> updatePostStatus(HttpServletRequest request,
                                          @PathVariable Long id,
                                          @RequestBody java.util.Map<String, Integer> body) {
        Long adminId = (Long) request.getAttribute("userId");
        Integer status = body.get("status");
        adminService.updatePostStatus(id, status);
        saveLog(adminId, "修改帖子状态", "PUT", "/admin/posts/" + id + "/status", request);
        return Result.success();
    }

    /**
     * 帖子置顶/取消置顶
     */
    @PutMapping("/posts/{id}/top")
    public Result<Void> updatePostTop(HttpServletRequest request,
                                        @PathVariable Long id,
                                        @RequestBody java.util.Map<String, Integer> body) {
        Long adminId = (Long) request.getAttribute("userId");
        Integer isTop = body.get("isTop");
        adminService.updatePostTop(id, isTop);
        saveLog(adminId, "修改帖子置顶状态", "PUT", "/admin/posts/" + id + "/top", request);
        return Result.success();
    }

    /**
     * 帖子加精/取消加精
     */
    @PutMapping("/posts/{id}/essence")
    public Result<Void> updatePostEssence(HttpServletRequest request,
                                            @PathVariable Long id,
                                            @RequestBody java.util.Map<String, Integer> body) {
        Long adminId = (Long) request.getAttribute("userId");
        Integer isEssence = body.get("isEssence");
        adminService.updatePostEssence(id, isEssence);
        saveLog(adminId, "修改帖子加精状态", "PUT", "/admin/posts/" + id + "/essence", request);
        return Result.success();
    }

    /**
     * 公告列表
     */
    @GetMapping("/notices")
    public Result<PageResult<SystemNotice>> getNoticeList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SystemNotice> result = adminService.getNoticeList(page, size);
        return Result.success(PageResult.of(result));
    }

    /**
     * 发布公告
     */
    @PostMapping("/notices")
    public Result<Void> publishNotice(HttpServletRequest request,
                                       @RequestBody @Valid PublishNoticeRequest noticeRequest) {
        Long adminId = (Long) request.getAttribute("userId");
        adminService.publishNotice(adminId, noticeRequest);
        saveLog(adminId, "发布公告", "POST", "/admin/notices", request);
        return Result.success();
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/notices/{id}")
    public Result<Void> deleteNotice(HttpServletRequest request,
                                       @PathVariable Long id) {
        Long adminId = (Long) request.getAttribute("userId");
        adminService.deleteNotice(id);
        saveLog(adminId, "删除公告", "DELETE", "/admin/notices/" + id, request);
        return Result.success();
    }

    /**
     * 更新公告
     */
    @PutMapping("/notices/{id}")
    public Result<Void> updateNotice(HttpServletRequest request,
                                      @PathVariable Long id,
                                      @RequestBody @Valid PublishNoticeRequest noticeRequest) {
        Long adminId = (Long) request.getAttribute("userId");
        adminService.updateNotice(id, noticeRequest);
        saveLog(adminId, "更新公告", "PUT", "/admin/notices/" + id, request);
        return Result.success();
    }

    /**
     * 操作日志列表
     */
    @GetMapping("/logs")
    public Result<PageResult<OperationLog>> getLogList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        Page<OperationLog> result = adminService.getLogList(page, size, keyword);
        return Result.success(PageResult.of(result));
    }

    // ========== 操作日志记录 ==========

    private void saveLog(Long adminId, String operation, String method,
                         String path, HttpServletRequest request) {
        try {
            OperationLog log = new OperationLog();
            log.setAdminId(adminId);
            log.setOperation(operation);
            log.setMethod(method + " " + path);
            log.setIp(getClientIp(request));
            log.setCreateTime(LocalDateTime.now());
            operationLogMapper.insert(log);
        } catch (Exception ignored) {
            // 日志记录失败不影响主业务
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
