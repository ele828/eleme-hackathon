package io.github.yfwz100.eleme.hack2015.database;

import io.github.yfwz100.eleme.hack2015.models.Cart;
import io.github.yfwz100.eleme.hack2015.models.Order;
import io.github.yfwz100.eleme.hack2015.models.AuthorizedUser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eric on 15/11/12.
 */
public class Cache {
    private static Map<String, AuthorizedUser> userMap = new HashMap<>();
    private static Map<String, Cart> cartMap = new HashMap<>();
    private static Map<String, Order> orderMap = new HashMap<>();

    public static void addUser(String accessToken, AuthorizedUser authorizedUser) {
        userMap.put(accessToken, authorizedUser);
    }

    public static AuthorizedUser getUser(String accessToken) {
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
