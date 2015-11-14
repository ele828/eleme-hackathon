package io.github.yfwz100.eleme.hack2015.exceptions;

/**
 * Created by Eric on 15/11/12.
 */
public class NoAccessToCartException extends Exception {
    public NoAccessToCartException() {
    }

    public NoAccessToCartException(String message) {
        super(message);
    }

    public NoAccessToCartException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoAccessToCartException(Throwable cause) {
        super(cause);
    }

    public NoAccessToCartException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
