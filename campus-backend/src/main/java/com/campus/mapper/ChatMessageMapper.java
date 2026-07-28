package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.ChatMessage;
import com.campus.vo.ChatMessageVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 获取与某用户的聊天记录（含双方昵称、头像），按时间正序
     */
    List<ChatMessageVO> selectChatHistory(@Param("me") Long me, @Param("other") Long other);

    /**
     * 查询当前用户的未读私聊总数
     */
    @Select("SELECT COUNT(*) FROM chat_message WHERE to_user_id = #{userId} AND is_read = 0")
    int countUnread(@Param("userId") Long userId);

    /**
     * 标记某用户发来的所有未读消息为已读
     */
    @Update("UPDATE chat_message SET is_read = 1 WHERE from_user_id = #{fromUserId} AND to_user_id = #{me} AND is_read = 0")
    int markAsRead(@Param("fromUserId") Long fromUserId, @Param("me") Long me);
}
