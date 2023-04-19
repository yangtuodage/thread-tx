package com.tempura.threadtx.test.controller;

import cn.hutool.core.util.RandomUtil;
import com.google.common.collect.Lists;
import com.tempura.threadtx.core.ThreadTxBuilder;
import com.tempura.threadtx.test.domain.CarInfo;
import com.tempura.threadtx.test.mapper.CarInfoMapper;
import com.tempura.threadtx.test.service.BatchInsertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author wzs
 * @since 2023/4/18 19:22
 */
@RestController
@RequestMapping("/testThreadTx")
@Slf4j
public class TestThreadTxController {

    @Resource
    private BatchInsertService batchInsertService;

    @Resource
    private CarInfoMapper carInfoMapper;

    @PutMapping("/test")
    public void test(int command){
        carInfoMapper.deleteAll();
        switch (command){
            case 1:
                batchInsertService.normalTx(Lists.newArrayList(new CarInfo("大众")));
                break;
            case 2:
                batchInsertService.threadTxTimeout(Lists.newArrayList(new CarInfo("大众")));
                break;
            case 3:
                batchInsertService.springTxTimeout(Lists.newArrayList(new CarInfo("大众")));
                break;
            case 4:
                batchInsertService.rollback(Lists.newArrayList(new CarInfo("大众")));
                break;
            case 5:
                batchInsertService.propagation(Lists.newArrayList(new CarInfo("大众")));
                break;
            case 6:
                batchInsertService.notPropagation(Lists.newArrayList(new CarInfo("大众")));
                break;
            default:
                break;
        }
    }

    @PutMapping("/largeDataInsert")
    public void largeDataInsert(int command){
        List<CarInfo> carInfos = Lists.newArrayList();
        for (int i = 0; i < 10000; i++) {
            carInfos.add(new CarInfo(RandomUtil.randomString(10)));
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        if(command == 1){
            batchInsertService.normalTx(carInfos);
        }else{
            List<List<CarInfo>> partitionList = Lists.partition(carInfos, 1000);
            ThreadTxBuilder threadTxBuilder = new ThreadTxBuilder()
                    .setThreadTxTimeOut(10);
            for (List<CarInfo> partition : partitionList) {
                threadTxBuilder.addTask(() -> batchInsertService.normalTx(partition));
            }
            threadTxBuilder.execute();
        }
        stopWatch.stop();
        log.info("执行完毕，耗时-------------------{}",stopWatch.getTotalTimeMillis());


    }

    /**
     * 多线程事务超时
     */
    @PutMapping("/threadTxTimeout")
    public void threadTxTimeout(){
        carInfoMapper.deleteAll();
        new ThreadTxBuilder()
                .setThreadTxTimeOut(3)
                .addTask(() -> batchInsertService.normalTx(Lists.newArrayList(new CarInfo("本田"))))
                .addTask(() -> batchInsertService.normalTx(Lists.newArrayList(new CarInfo("丰田"))))
                .addTask(() -> batchInsertService.threadTxTimeout(Lists.newArrayList(new CarInfo("大众"))))
                .execute();
    }


    /**
     * spring事务超时
     */
    @PutMapping("/springTxTimeout")
    public void springTxTimeout(){
        carInfoMapper.deleteAll();
        new ThreadTxBuilder()
                .addTask(() -> batchInsertService.normalTx(Lists.newArrayList(new CarInfo("本田"))))
                .addTask(() -> batchInsertService.normalTx(Lists.newArrayList(new CarInfo("丰田"))))
                .addTask(() -> batchInsertService.springTxTimeout(Lists.newArrayList(new CarInfo("大众"))))
                .execute();
    }

    /**
     * 事务回滚
     */
    @PutMapping("/rollback")
    public void rollback(){
        carInfoMapper.deleteAll();
        new ThreadTxBuilder()
                .addTask(() -> batchInsertService.normalTx(Lists.newArrayList(new CarInfo("本田"))))
                .addTask(() -> batchInsertService.normalTx(Lists.newArrayList(new CarInfo("丰田"))))
                .addTask(() -> batchInsertService.rollback(Lists.newArrayList(new CarInfo("大众"))))
                .execute();
    }

    /**
     * 事务传播导致回滚
     */
    @PutMapping("/propagation")
    public void propagation(){
        carInfoMapper.deleteAll();
        new ThreadTxBuilder()
                .addTask(() -> batchInsertService.normalTx(Lists.newArrayList(new CarInfo("本田"))))
                .addTask(() -> batchInsertService.normalTx(Lists.newArrayList(new CarInfo("丰田"))))
                .addTask(() -> batchInsertService.propagation(Lists.newArrayList(new CarInfo("大众"))))
                .execute();
    }

    /**
     * 事务不传播导致不回滚
     */
    @PutMapping("/notPropagation")
    public void notPropagation(){
        carInfoMapper.deleteAll();
        new ThreadTxBuilder()
                .addTask(() -> batchInsertService.normalTx(Lists.newArrayList(new CarInfo("本田"))))
                .addTask(() -> batchInsertService.normalTx(Lists.newArrayList(new CarInfo("丰田"))))
                .addTask(() -> batchInsertService.notPropagation(Lists.newArrayList(new CarInfo("大众"))))
                .execute();
    }

    /**
     * 事务与非事务一起执行
     */
    @PutMapping("/mixTx")
    public void mixTx(){
        carInfoMapper.deleteAll();
        new ThreadTxBuilder()
                .addTask(() -> carInfoMapper.batchInsert(Lists.newArrayList(new CarInfo("本田"))))
                .addTask(() -> batchInsertService.normalTx(Lists.newArrayList(new CarInfo("丰田"))))
                .addTask(() -> batchInsertService.rollback(Lists.newArrayList(new CarInfo("大众"))))
                .execute();
    }
}
