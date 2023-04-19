package com.tempura.threadtx.core;

/**
 * @author wzs
 * @since 2023/4/18 17:24
 */
public class ThreadTxException extends RuntimeException {

    public ThreadTxException(String msg) {
        super(msg);
    }
}
