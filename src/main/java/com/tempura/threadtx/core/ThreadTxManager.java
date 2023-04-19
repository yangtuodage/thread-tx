package com.tempura.threadtx.core;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.DefaultTransactionStatus;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * @author wzs
 * @since 2023/4/17 22:02
 */
@Component
@Slf4j
public class ThreadTxManager extends DataSourceTransactionManager {

    public ThreadTxManager(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    @SneakyThrows
    protected void prepareForCommit(@NonNull DefaultTransactionStatus status) {
        ThreadTxInfo threadTxInfo = ThreadTxBuilder.getThreadTxInfo();
        // 有挂起的事务则不处理
        if(Objects.nonNull(status.getSuspendedResources())){
            return;
        }

        // 没有多线程事务则不处理
        if(Objects.isNull(threadTxInfo)){
            return;
        }

        // 如果当前线程需要回滚则标记全局回滚
        if(unexpectedRollback(status)){
            threadTxInfo.getGlobalThreadRollBack().set(Boolean.TRUE);
            return;
        }

        // 自身任务完成等待主线程唤醒
        threadTxInfo.getWorkerThreadLatch().countDown();
        threadTxInfo.getMasterThreadLatch().await();

        // 如果需要全局回滚则回滚掉当前线程的事务
        if(threadTxInfo.isGlobalThreadRollback()){
            throw new ThreadTxException("global thread transaction rollback");
        }
    }

    private boolean unexpectedRollback(DefaultTransactionStatus status) {
        if(status.hasSavepoint() ||
                status.isNewTransaction() ||
                isFailEarlyOnGlobalRollbackOnly()){
            return status.isGlobalRollbackOnly();
        }
        return false;
    }

    @Override
    protected void doRollback(@NonNull DefaultTransactionStatus status) {
        // 挂起的事务为空，存在多线程事务，则设置多线程事务回滚状态
        ThreadTxInfo threadTxInfo = ThreadTxBuilder.getThreadTxInfo();
        if(Objects.isNull(status.getSuspendedResources()) && Objects.nonNull(threadTxInfo)){
            ThreadTxBuilder.getThreadTxInfo().getGlobalThreadRollBack().set(Boolean.TRUE);
            ThreadTxBuilder.getThreadTxInfo().getWorkerThreadLatch().countDown();
        }
        super.doRollback(status);
    }
}
