package io.github.yfwz100.eleme.hack2015.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The cart entity.
 *
 * @author eric
 * @author yfwz100
 */
public class Cart {

    private final String cartId;
    private final Map<Integer, AtomicInteger> menu;
    private final Session session;
    private int count = 0;

    public Cart(Session session) {
        this(session, new HashMap<>());
    }

    public Cart(Session session, Map<Integer, AtomicInteger> menu) {
        this.session = session;
        this.cartId = UUID.randomUUID().toString();
        this.menu = new HashMap<>(3);
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
        return count;
    }

    public int addFood(Food food, int count) {
        this.count += count;
        return menu.computeIfAbsent(food.getId(), i -> new AtomicInteger()).addAndGet(count);
    }

}