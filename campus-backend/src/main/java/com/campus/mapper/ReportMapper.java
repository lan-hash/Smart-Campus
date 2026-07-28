package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.entity.Report;
import com.campus.vo.ReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReportMapper extends BaseMapper<Report> {

    Page<ReportVO> selectReportPage(Page<?> page, @Param("status") Integer status);

    @Select("SELECT COUNT(*) FROM report WHERE status = 0")
    long countPendingReports();
}
