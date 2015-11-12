package io.github.yfwz100.eleme.hack2015.exceptions;

/**
 * Created by Eric on 15/11/12.
 */
public class FoodOutOfLimitException extends Exception {
    public FoodOutOfLimitException() {
    }

    public FoodOutOfLimitException(String message) {
        super(message);
    }

    public FoodOutOfLimitException(String message, Throwable cause) {
        super(message, cause);
    }

    public FoodOutOfLimitException(Throwable cause) {
        super(cause);
    }

    public FoodOutOfLimitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
