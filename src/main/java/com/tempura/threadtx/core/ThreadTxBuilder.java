package com.tempura.threadtx.core;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NamedThreadLocal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wzs
 * @since 2023/4/16 17:12
 */
@Slf4j
public class ThreadTxBuilder{

    private static final String THREAD_NAME_PREFIX = "threadTx-exec-";

    private final AtomicInteger threadNumber = new AtomicInteger(1);


    /**
     * 全局事务超时时间
     */
    private int threadTxTimeOut = 120;

    /**
     * 所有异步事务
     */
    private final List<Runnable> tasks = new ArrayList<>();

    /**
     * 事务信息
     */
    private static final ThreadLocal<ThreadTxInfo> ThreadTxInfo =
            new NamedThreadLocal<>("thread transaction info");

    public static ThreadTxInfo getThreadTxInfo(){
        return ThreadTxInfo.get();
    }

    public ThreadTxBuilder setThreadTxTimeOut(int timeOut){
        this.threadTxTimeOut = timeOut;
        return this;
    }

    public ThreadTxBuilder addTask(Runnable runnable){
        tasks.add(runnable);
        return this;
    }

    public ThreadTxBuilder addTasks(Runnable... multiRunnable){
        tasks.addAll(Arrays.asList(multiRunnable));
        return this;
    }

    @SneakyThrows
    public void execute(){
        // 工作线程监控
        CountDownLatch workerThreadLatch;
        CountDownLatch masterThreadLatch = new CountDownLatch(1);
        // 全局事务上下文
        ThreadTxInfo threadTxInfo = new ThreadTxInfo();
        threadTxInfo.setGlobalThreadRollBack(new AtomicBoolean(Boolean.FALSE));
        threadTxInfo.setMasterThreadLatch(masterThreadLatch);
        workerThreadLatch = new CountDownLatch(tasks.size());
        threadTxInfo.setWorkerThreadLatch(workerThreadLatch);

        // 开始执行多线程事务
        for (Runnable task : tasks) {
            new Thread(() -> {
                ThreadTxInfo.set(threadTxInfo);
                try{
                    task.run();
                    // 如果该线程没有执行过事务
                    if(masterThreadLatch.getCount() > 0){
                        threadTxInfo.getWorkerThreadLatch().countDown();
                    }
                }finally {
                    ThreadTxInfo.remove();
                }
            },THREAD_NAME_PREFIX +  threadNumber.getAndIncrement()).start();
        }

        // 等待所有工作线程完成任务，如果等待超时则主动回滚
        if(!threadTxInfo.getWorkerThreadLatch().await(this.threadTxTimeOut,TimeUnit.SECONDS)){
            log.error("global thread transaction timed out");
            threadTxInfo.getGlobalThreadRollBack().set(Boolean.TRUE);
        }

        // 唤醒所有工作线程提交或回滚事务
        masterThreadLatch.countDown();
    }
}
