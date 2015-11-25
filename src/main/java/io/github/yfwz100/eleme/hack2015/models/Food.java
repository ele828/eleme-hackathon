package io.github.yfwz100.eleme.hack2015.models;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Food Model.
 *
 * @author yfwz100
 */
public class Food {

    private final int id;
    private final int price;
    private final AtomicInteger stock;

    public Food(int id, int price, int stock) {
        this.id = id;
        this.price = price;
        this.stock = new AtomicInteger(stock);
    }

    public int getId() {
        return id;
    }

    public int getPrice() {
        return price;
    }

    public int getStock() {
        return stock.get();
    }

    public int consumeStock(int quantity) {
        return this.stock.addAndGet(-quantity);
    }

    @Override
    public String toString() {
        return String.format("Food{id=%d, price=%s, stock=%s}", id, price, stock);
    }
}
