package io.github.yfwz100.eleme.hack2015.services;

import io.github.yfwz100.eleme.hack2015.services.exceptions.CartNotFoundException;
import io.github.yfwz100.eleme.hack2015.services.exceptions.FoodNotFoundException;
import io.github.yfwz100.eleme.hack2015.services.exceptions.FoodOutOfLimitException;
import io.github.yfwz100.eleme.hack2015.services.exceptions.NoAccessToCartException;
import io.github.yfwz100.eleme.hack2015.models.Cart;

/**
 * Service interface for carts.
 *
 * @author yfwz100
 */
public interface CartsService {

    /**
     * Create a new cart according to the access token.
     *
     * @param accessToken the access token.
     * @return the cart.
     */
    Cart createCart(String accessToken);

    /**
     * Add food to cart.
     *
     * @param accessToken the access token.
     * @param cartId      the id of the cart.
     * @param foodId      the id of food.
     * @param count       the count of the food.
     * @throws FoodOutOfLimitException if no food is available.
     * @throws FoodNotFoundException   if food id is not valid.
     * @throws CartNotFoundException   if cart is not valid.
     * @throws NoAccessToCartException if access token is not valid.
     */
    void addFoodToCart(String accessToken, String cartId, int foodId, int count)
            throws FoodOutOfLimitException, FoodNotFoundException, CartNotFoundException, NoAccessToCartException;
}
