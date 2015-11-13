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
    private User user;

    public Cart() {}
    public Cart(User user) {
        this.user = user;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        user = user;
    }

    public boolean checkAvailable(int count) {
        if (count + foods.size() > maxSize)
            return false;
        return true;
    }

    public boolean addFood(Food food, int count) {
        if (food == null)
            return false;
        if (foods.size() >= maxSize)
            return false;
        return foods.add(food);
    }

}