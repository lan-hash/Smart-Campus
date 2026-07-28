package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.CourseEvaluation;
import com.campus.vo.CourseEvaluationVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CourseEvaluationMapper extends BaseMapper<CourseEvaluation> {

    /**
     * 查询某订单的评价列表，关联评价者用户信息
     */
    @Select("SELECT e.id, e.order_id AS orderId, e.from_user_id AS fromUserId, " +
            "e.to_user_id AS toUserId, e.score, e.content, e.create_time AS createTime, " +
            "u.nickname AS fromUserNickname, u.avatar AS fromUserAvatar " +
            "FROM course_evaluation e " +
            "LEFT JOIN sys_user u ON e.from_user_id = u.id " +
            "WHERE e.order_id = #{orderId} " +
            "ORDER BY e.create_time DESC")
    List<CourseEvaluationVO> selectEvaluationsByOrderId(Long orderId);
}
