package io.github.yfwz100.eleme.hack2015.models;


import java.util.*;

/**
 * The order model.
 *
 * @author yfwz100
 */
public class Order {

    private final String orderId;
    private final Map<Integer, Integer> menu;
    private final User user;

    public Order(User user, Map<Integer, Integer> menu) {
        this.orderId = UUID.randomUUID().toString();
        this.menu = menu;
        this.user = user;
    }

    public String getOrderId() {
        return orderId;
    }

    public Map<Integer, Integer> getMenu() {
        return menu;
    }

    public User getUser() {
        return user;
    }
}
