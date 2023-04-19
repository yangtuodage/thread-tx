package com.tempura.threadtx.test.mapper;

import com.tempura.threadtx.test.domain.CarInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;

/**
 * @author wzs
 * @since 2023/4/18 19:24
 */
@Mapper
public interface CarInfoMapper{
    /**
     * 数据插入
     * @param carInfos 数据
     */
    void batchInsert(@Param("carInfos") Collection<CarInfo> carInfos);

    /**
     * 清空数据
     */
    void deleteAll();

    /**
     * 执行sql
     * @param sql sql语句
     */
    void execute(@Param("sql")String sql);
}
