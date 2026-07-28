package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.entity.Notification;
import com.campus.vo.NotificationVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 分页查询用户的通知列表（LEFT JOIN sys_user 获取发送者昵称/头像），按时间倒序
     */
    Page<NotificationVO> selectNotificationPage(Page<NotificationVO> page, @Param("userId") Long userId);

    /**
     * 查询未读通知数量
     */
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0")
    int countUnread(@Param("userId") Long userId);

    /**
     * 标记所有通知为已读
     */
    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);

    /**
     * 标记单条通知为已读
     */
    @Update("UPDATE notification SET is_read = 1 WHERE id = #{notificationId} AND user_id = #{userId} AND is_read = 0")
    int markAsRead(@Param("userId") Long userId, @Param("notificationId") Long notificationId);
}
