package com.tempura.threadtx.test.service;

import com.tempura.threadtx.test.domain.CarInfo;
import com.tempura.threadtx.test.mapper.CarInfoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author wzs
 * @since 2023/4/18 19:25
 */
@Service
public class CarInfoService {

    @Resource
    private CarInfoMapper carInfoMapper;

    @Transactional(rollbackFor = Exception.class)
    public void batchInsert(List<CarInfo> carInfos){
        carInfoMapper.batchInsert(carInfos);

        // 模拟异常
        for (CarInfo carInfo : carInfos) {
            if(Objects.equals("CarInfoService抛出异常",carInfo.getCarName())){
                int i = 1 / 0;
            }
        }
    }

    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    public void propagation(List<CarInfo> carInfos) {
        carInfoMapper.batchInsert(carInfos);
        int i = 1 / 0;
    }

    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRES_NEW)
    public void notPropagation(List<CarInfo> carInfos) {
        carInfoMapper.batchInsert(carInfos);
        int i = 1 / 0;
    }
}
