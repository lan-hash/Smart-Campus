package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.AiChatRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface AiChatRecordMapper extends BaseMapper<AiChatRecord> {

    /**
     * 获取用户所有会话列表（sessionId + 最后一条消息 + 最后时间）
     */
    @Select("SELECT DISTINCT session_id AS sessionId, MAX(create_time) AS lastMessageTime " +
            "FROM ai_chat_record WHERE user_id = #{userId} " +
            "GROUP BY session_id ORDER BY MAX(create_time) DESC")
    List<Map<String, Object>> selectSessions(@Param("userId") Long userId);

    /**
     * 获取某会话最后一条消息内容
     */
    @Select("SELECT content FROM ai_chat_record WHERE session_id = #{sessionId} " +
            "AND user_id = #{userId} ORDER BY create_time DESC LIMIT 1")
    String selectLastMessage(@Param("userId") Long userId, @Param("sessionId") String sessionId);

    /**
     * 删除会话所有消息
     */
    @Delete("DELETE FROM ai_chat_record WHERE user_id = #{userId} AND session_id = #{sessionId}")
    int deleteBySession(@Param("userId") Long userId, @Param("sessionId") String sessionId);
}
