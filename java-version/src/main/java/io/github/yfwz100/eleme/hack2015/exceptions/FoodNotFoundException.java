package io.github.yfwz100.eleme.hack2015.exceptions;

/**
 * Created by Eric on 15/11/12.
 */
public class FoodNotFoundException extends Exception {
    public FoodNotFoundException() {
    }

    public FoodNotFoundException(String message) {
        super(message);
    }

    public FoodNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public FoodNotFoundException(Throwable cause) {
        super(cause);
    }

    public FoodNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
