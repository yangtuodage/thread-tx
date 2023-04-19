package com.tempura.threadtx.core;

import lombok.Data;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author wzs
 * @since 2023/4/16 17:14
 */
@Data
public class ThreadTxInfo {

    /**
     * 是否全局线程回滚
     */
    private AtomicBoolean globalThreadRollBack;

    /**
     * 主线程监控
     */
    private CountDownLatch masterThreadLatch;

    /**
     * 工作线程监控
     */
    private CountDownLatch workerThreadLatch;


    public boolean isGlobalThreadRollback(){
        return Objects.nonNull(this.globalThreadRollBack) && Boolean.TRUE.equals(this.globalThreadRollBack.get());
    }
}
