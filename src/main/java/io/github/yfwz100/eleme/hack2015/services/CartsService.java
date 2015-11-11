package io.github.yfwz100.eleme.hack2015.services;

/**
 * The carts service.
 */
public interface CartsService {

    /**
     * Get the corresponding cart id for given access token.
     *
     * @param accessToken the access token.
     * @return the cart Id.
     */
    String getCartId(String accessToken);

    /**
     * Add food to the cart.
     *
     * @param foodId the food id.
     * @param count  the quantity.
     * @throws CartsServiceException
     */
    void addFood(int foodId, int count) throws CartsServiceException;

    /**
     * Generate the order from given cart id.
     *
     * @param cartId the id of the cart.
     * @throws CartsServiceException
     */
    void generateOrder(String cartId) throws CartsServiceException;

    /**
     * The root exception for carts service.
     */
    class CartsServiceException extends Exception {
    }

    /**
     * The given cart not found.
     */
    class CartsServiceNotFoundException extends CartsServiceException {
    }

    /**
     * The user is not authorized to access the cart.
     */
    class UnauhorizedAccessException extends CartsServiceException {
    }

    /**
     * Food is out of limit.
     */
    class FoodOutOfLimitException extends CartsServiceException {
    }

    /**
     * Food is not found.
     */
    class FoodNotFoundException extends CartsServiceException {
    }
}
