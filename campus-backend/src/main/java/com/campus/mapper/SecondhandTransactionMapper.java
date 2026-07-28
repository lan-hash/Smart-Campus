package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.entity.SecondhandTransaction;
import com.campus.vo.SecondhandTransactionVO;
import org.apache.ibatis.annotations.Param;

public interface SecondhandTransactionMapper extends BaseMapper<SecondhandTransaction> {

    Page<SecondhandTransactionVO> selectMyTransactions(Page<?> page, @Param("userId") Long userId);
}
