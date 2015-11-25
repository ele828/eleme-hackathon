package io.github.yfwz100.eleme.hack2015.services;

import io.github.yfwz100.eleme.hack2015.models.*;
import io.github.yfwz100.eleme.hack2015.services.exceptions.FoodOutOfStockException;

import java.util.Collection;

/**
 * The interface of cache layer.
 *
 * @author yfwz100
 */
public interface Cache {
    /**
     * Add session to cache.
     *
     * @param session the session to store.
     */
    void addSession(Session session);

    /**
     * Get session by access token.
     *
     * @param accessToken the acces token.
     * @return the session.
     */
    Session getSession(String accessToken);

    /**
     * MemoryPoolCache the cart.
     *
     * @param cart the cart.
     */
    void addCart(Cart cart);

    /**
     * Get cart by cart id.
     *
     * @param cid the cart id.
     * @return the cart.
     */
    Cart getCart(String cid);

    /**
     * Add order.
     *
     * @param order the order.
     */
    void addOrder(Order order);

    /**
     * Get order by id.
     *
     * @param oId the id of order.
     * @return the order.
     */
    Order getOrder(String oId);

    /**
     * Get user by name.
     *
     * @param name the username.
     * @return the user.
     */
    User getUser(String name);

    /**
     * Get food by id.
     *
     * @param foodId the food id.
     * @return the food.
     */
    Food getFood(int foodId);

    /**
     * Get foods.
     *
     * @return the collection of foods.
     */
    Collection<Food> getFoods();

    /**
     * Consume the food by ID.
     *
     * @param id       the food ID.
     * @param quantity the quantity.
     */
    int consumeFood(int id, int quantity) throws FoodOutOfStockException;
}
