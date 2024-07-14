package com.t13max.game.exception;

/**
 * @author: t13max
 * @since: 14:11 2024/5/23
 */
public class CommonException extends RuntimeException {

    public CommonException(String message) {
        super(message);
    }

    public CommonException(Throwable cause) {
        super(cause);
    }
}
