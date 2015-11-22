package io.github.yfwz100.eleme.hack2015.database;

import io.github.yfwz100.eleme.hack2015.models.Cart;
import io.github.yfwz100.eleme.hack2015.models.Order;
import io.github.yfwz100.eleme.hack2015.models.Session;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The cache layer.
 *
 * @author eric
 */
public class Cache {
    private static Map<String, Session> userMap = new ConcurrentHashMap<>();
    private static Map<String, Cart> cartMap = new ConcurrentHashMap<>();
    private static Map<String, Order> orderMap = new ConcurrentHashMap<>();

    /**
     * Add session to cache.
     *
     * @param accessToken the access token.
     * @param session     the session to store.
     */
    public static void addSession(Session session) {
        userMap.put(session.getAccessToken(), session);
    }

    /**
     * Get session by access token.
     *
     * @param accessToken the acces token.
     * @return the session.
     */
    public static Session getSession(String accessToken) {
        return userMap.get(accessToken);
    }

    /**
     * Cache the cart.
     *
     * @param cart the cart.
     */
    public static void addCart(Cart cart) {
        cartMap.put(cart.getCartId(), cart);
    }

    /**
     * Get cart by cart id.
     *
     * @param cid the cart id.
     * @return the cart.
     */
    public static Cart getCart(String cid) {
        return cartMap.get(cid);
    }

    /**
     * Add order.
     *
     * @param order the order.
     */
    public static void addOrder(Order order) {
        orderMap.put(order.getOrderId(), order);
    }

    /**
     * Get order by id.
     *
     * @param oId the id of order.
     * @return the order.
     */
    public static Order getOrder(String oId) {
        return orderMap.get(oId);
    }

}
