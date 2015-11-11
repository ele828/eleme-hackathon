package io.github.yfwz100.eleme.hack2015.services;

import io.github.yfwz100.eleme.hack2015.models.Order;

import java.util.List;

/**
 * The services for orders.
 *
 * @author yfwz100
 */
public interface OrdersService {

    /**
     * Get orders of a single user.
     *
     * @param user the Id of the user.
     * @return the list of orders.
     */
    List<Order> getOrdersByUser(int user);

    /**
     * Get all orders.
     *
     * @return the list of orders.
     */
    List<Order> getAllOrders();
}
