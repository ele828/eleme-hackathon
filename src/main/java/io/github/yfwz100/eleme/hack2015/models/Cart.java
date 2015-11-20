package io.github.yfwz100.eleme.hack2015.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Eric on 15/11/12.
 */
public class Cart {
    private final int maxSize = 3;
    private String cartId;
    private List<Food> foods = new ArrayList<>(3);
    private AuthorizedUser authorizedUser;

    public Cart() {
    }

    public Cart(AuthorizedUser authorizedUser) {
        this.authorizedUser = authorizedUser;
        cartId = UUID.randomUUID().toString();
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public List<Food> getFoods() {
        return foods;
    }

    public void setFoods(List<Food> foods) {
        this.foods = foods;
    }

    public AuthorizedUser getUser() {
        return authorizedUser;
    }

    public void setUser(AuthorizedUser authorizedUser) {
        authorizedUser = authorizedUser;
    }

    public boolean checkAvailable(int count) {
        return count + foods.size() <= maxSize;
    }

    public boolean addFood(Food food, int count) {
        return food != null && foods.size() < maxSize && foods.add(food);
    }

}