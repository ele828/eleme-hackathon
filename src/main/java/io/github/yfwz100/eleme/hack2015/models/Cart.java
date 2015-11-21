package io.github.yfwz100.eleme.hack2015.models;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The cart entity.
 *
 * @author eric
 * @author yfwz100
 */
public class Cart {

    private final String cartId;
    private final Map<Integer, AtomicInteger> menu = new HashMap<>(3);
    private final Session session;

    public Cart(Session session) {
        this.session = session;
        this.cartId = UUID.randomUUID().toString();
    }

    public String getCartId() {
        return cartId;
    }

    public Set<Map.Entry<Integer, AtomicInteger>> getMenu() {
        return menu.entrySet();
    }

    public Session getSession() {
        return session;
    }

    public int getSize() {
        return menu.values().stream().mapToInt(AtomicInteger::get).sum();
    }

    public int addFood(Food food, int count) {
        return menu.computeIfAbsent(food.getId(), i -> new AtomicInteger()).addAndGet(count);
    }

}