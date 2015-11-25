package io.github.yfwz100.eleme.hack2015.services;

import io.github.yfwz100.eleme.hack2015.models.Order;
import io.github.yfwz100.eleme.hack2015.services.exceptions.CartNotFoundException;
import io.github.yfwz100.eleme.hack2015.services.exceptions.FoodOutOfStockException;
import io.github.yfwz100.eleme.hack2015.services.exceptions.NoAccessToCartException;
import io.github.yfwz100.eleme.hack2015.services.exceptions.OrderOutOfLimitException;

import java.util.Collection;

/**
 * The service interface for orders.
 *
 * @author eric
 * @author yfwz100
 */
public interface OrdersService {

    /**
     * Generate the order by given access token and cart id.
     *
     * @param accessToken the access token.
     * @param cartId      the id of the cart.
     * @return the order.
     * @throws CartNotFoundException    if cart is not found.
     * @throws NoAccessToCartException  if access token is invalid.
     * @throws FoodOutOfStockException  if food is not available.
     * @throws OrderOutOfLimitException if order is out of limit.
     */
    Order generateOrder(String accessToken, String cartId)
            throws CartNotFoundException, NoAccessToCartException, FoodOutOfStockException, OrderOutOfLimitException;

    /**
     * Get the order by access token.
     *
     * @param accessToken the access token.
     * @return the order.
     */
    Order getOrder(String accessToken);

    /**
     * Get orders.
     */
    Collection<Order> getOrders();
}
