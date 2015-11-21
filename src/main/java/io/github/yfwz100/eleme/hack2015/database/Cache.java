package io.github.yfwz100.eleme.hack2015.database;

import io.github.yfwz100.eleme.hack2015.models.Cart;
import io.github.yfwz100.eleme.hack2015.models.Order;
import io.github.yfwz100.eleme.hack2015.models.Session;

import java.util.HashMap;
import java.util.Map;

/**
 * The cache layer.
 *
 * @author eric
 */
public class Cache {
    private static Map<String, Session> userMap = new HashMap<>();
    private static Map<String, Cart> cartMap = new HashMap<>();
    private static Map<String, Order> orderMap = new HashMap<>();

    public static void addSession(String accessToken, Session session) {
        userMap.put(accessToken, session);
    }

    public static Session getSession(String accessToken) {
        return userMap.get(accessToken);
    }

    public static void addCart(Cart cart) {
        cartMap.put(cart.getCartId(), cart);
    }

    public static Cart getCart(String cid) {
        return cartMap.get(cid);
    }

    public static void addOrder(Order order) {
        orderMap.put(order.getOrderId(), order);
    }

    public static Order getOrder(String oId) {
        return orderMap.get(oId);
    }

    public static Map<String, Order> getOrders() {
        return orderMap;
    }

}
