package io.github.yfwz100.eleme.hack2015.exceptions;

/**
 * Created by Eric on 15/11/12.
 */
public class FoodOutOfStockException extends Exception {
    public FoodOutOfStockException() {
    }

    public FoodOutOfStockException(String message) {
        super(message);
    }

    public FoodOutOfStockException(String message, Throwable cause) {
        super(message, cause);
    }

    public FoodOutOfStockException(Throwable cause) {
        super(cause);
    }

    public FoodOutOfStockException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
