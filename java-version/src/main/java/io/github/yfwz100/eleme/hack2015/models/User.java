package io.github.yfwz100.eleme.hack2015.models;

import io.github.yfwz100.eleme.hack2015.exceptions.OrderOutOfLimitException;

/**
 * Created by Eric on 15/11/12.
 */
public class User {
    private int id;
    private String name;
    private String pass;
    private String accessToken;
    private Order order;

    public User() {}

    public User(int id, String name, String pass, String accessToken) {
        this.id = id;
        this.name = name;
        this.pass = pass;
        this.accessToken = accessToken;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean canMakeOrder() {
        return order == null;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
