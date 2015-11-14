package io.github.yfwz100.eleme.hack2015.exceptions;

/**
 * Created by Eric on 15/11/13.
 */
public class OrderOutOfLimitException extends Exception {
    public OrderOutOfLimitException() {
    }

    public OrderOutOfLimitException(String message) {
        super(message);
    }

    public OrderOutOfLimitException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderOutOfLimitException(Throwable cause) {
        super(cause);
    }

    public OrderOutOfLimitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
