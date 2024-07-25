package com.t13max.game.exception;

/**
 * @author: t13max
 * @since: 15:20 2024/4/11
 */
public class GameException extends RuntimeException{

    public GameException(String message) {
        super(message);
    }

    public GameException(String message, Throwable cause) {
        super(message, cause);
    }

    public GameException(Throwable cause) {
        super(cause);
    }

    public GameException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public GameException() {
    }
}
