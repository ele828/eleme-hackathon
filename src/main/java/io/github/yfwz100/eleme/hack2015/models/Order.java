package io.github.yfwz100.eleme.hack2015.models;

import java.util.*;

/**
 * The order model.
 *
 * @author yfwz100
 */
public class Order {

    private String orderId;
    private int userId;
    private Map<Integer, Integer> items;

    public Order() {
        this(UUID.randomUUID().toString());
    }

    public Order(String orderId) {
        this(orderId, new HashMap<>());
    }

    public Order(String orderId, Map<Integer, Integer> items) {
        this.orderId = orderId;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Map<Integer, Integer> getItems() {
        return items;
    }

    public void setItems(Map<Integer, Integer> items) {
        this.items = items;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTotalItems() {
        return items.values().stream().mapToInt(e -> e).sum();
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", items=" + items +
                '}';
    }
}
