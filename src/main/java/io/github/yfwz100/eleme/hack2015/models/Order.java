package io.github.yfwz100.eleme.hack2015.models;


import java.util.*;

/**
 * The order model.
 *
 * @author yfwz100
 */
public class Order {
    private String orderId;
    private Session session;
    private List<Food> items;

    public Order() {}

    public Order(Session session, List<Food> items) {
        orderId = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.session = session;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public List<Food> getItems() {
        return items;
    }

    public void setItems(List<Food> items) {
        this.items = items;
    }
}
