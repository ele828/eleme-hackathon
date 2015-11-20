package io.github.yfwz100.eleme.hack2015.models;


import java.util.*;

/**
 * The order model.
 *
 * @author yfwz100
 */
public class Order {
    private String orderId;
    private AuthorizedUser authorizedUser;
    private List<Food> items;

    public Order() {}

    public Order(AuthorizedUser authorizedUser, List<Food> items) {
        orderId = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.authorizedUser = authorizedUser;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public AuthorizedUser getAuthorizedUser() {
        return authorizedUser;
    }

    public void setAuthorizedUser(AuthorizedUser authorizedUser) {
        this.authorizedUser = authorizedUser;
    }

    public List<Food> getItems() {
        return items;
    }

    public void setItems(List<Food> items) {
        this.items = items;
    }
}
