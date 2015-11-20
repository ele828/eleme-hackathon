package io.github.yfwz100.eleme.hack2015.models;

import io.github.yfwz100.eleme.hack2015.exceptions.OrderOutOfLimitException;

/**
 * The authorized user.
 *
 * @author yfwz100
 */
public class AuthorizedUser {

    private User user;
    private String accessToken;
    private Order order;

    public AuthorizedUser(User user, String accessToken) {
        this.user = user;
        this.accessToken = accessToken;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
