package io.github.yfwz100.eleme.hack2015.database;

import io.github.yfwz100.eleme.hack2015.models.Cart;
import io.github.yfwz100.eleme.hack2015.models.Order;
import io.github.yfwz100.eleme.hack2015.models.Session;
import io.github.yfwz100.eleme.hack2015.services.Cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The cache layer.
 *
 * @author eric
 */
public class MemoryBasedCache implements Cache {
    private Map<String, Session> userMap = new ConcurrentHashMap<>();
    private Map<String, Cart> cartMap = new ConcurrentHashMap<>();
    private Map<String, Order> orderMap = new ConcurrentHashMap<>();

    @Override
    public void addSession(Session session) {
        userMap.put(session.getAccessToken(), session);
    }

    @Override
    public Session getSession(String accessToken) {
        return userMap.get(accessToken);
    }

    @Override
    public void addCart(Cart cart) {
        cartMap.put(cart.getCartId(), cart);
    }

    @Override
    public Cart getCart(String cid) {
        return cartMap.get(cid);
    }

    @Override
    public void addOrder(Order order) {
        orderMap.put(order.getOrderId(), order);
    }

    @Override
    public Order getOrder(String oId) {
        return orderMap.get(oId);
    }

}
