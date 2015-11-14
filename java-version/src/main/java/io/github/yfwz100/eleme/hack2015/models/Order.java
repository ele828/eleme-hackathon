package io.github.yfwz100.eleme.hack2015.models;


import java.util.*;

/**
 * The order model.
 *
 * @author yfwz100
 */
public class Order {
    private String orderId;
    private User user;
    private List<Food> items;

    public Order() {}

    public Order(User user, List<Food> items) {
        orderId = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.user = user;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Food> getItems() {
        return items;
    }

    public void setItems(List<Food> items) {
        this.items = items;
    }
}
