package com.tempura.threadtx.test.service;

import cn.hutool.core.thread.ThreadUtil;
import com.tempura.threadtx.test.domain.CarInfo;
import com.tempura.threadtx.test.mapper.CarInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author wzs
 * @since 2023/4/18 19:24
 */
@Slf4j
@Component
public class BatchInsertService{

    @Resource
    private CarInfoService carInfoService;

    @Resource
    private CarInfoMapper carInfoMapper;


    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    public void normalTx(List<CarInfo> carInfos) {
        carInfoMapper.batchInsert(carInfos);
    }

    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    public void threadTxTimeout(List<CarInfo> carInfos){
        carInfoMapper.batchInsert(carInfos);
        ThreadUtil.sleep(10, TimeUnit.SECONDS);

    }



    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED,timeout = 3)
    public void springTxTimeout(List<CarInfo> carInfos){
        carInfoMapper.batchInsert(carInfos);
        carInfoMapper.execute("select sleep(10)");

    }



    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    public void rollback(List<CarInfo> carInfos){
        carInfoMapper.batchInsert(carInfos);
        int i = 1 / 0;
    }


    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    public void propagation(List<CarInfo> carInfos){
        try{
            carInfoService.propagation(carInfos);
        }catch (Exception e){
            log.error("异常",e);
        }
        carInfoMapper.batchInsert(carInfos);

    }


    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    public void notPropagation(List<CarInfo> carInfos){
        try{
            carInfoService.notPropagation(carInfos);
        }catch (Exception e){
            log.error("异常",e);
        }
        carInfoMapper.batchInsert(carInfos);

    }


}
