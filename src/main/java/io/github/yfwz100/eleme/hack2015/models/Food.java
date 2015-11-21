package io.github.yfwz100.eleme.hack2015.models;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Food Model.
 *
 * @author yfwz100
 */
public class Food {

    private int id;
    private double price;
    private AtomicInteger stock;
    private int count;

    public Food() {
    }

    public Food(int id, double price, int stock) {
        this.id = id;
        this.price = price;
        this.stock = new AtomicInteger(stock);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock.get();
    }

    public AtomicInteger getStockCounter() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock.set(stock);
    }

    public int consumeStock(int quantity) {
        return this.stock.addAndGet(-quantity);
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return String.format("Food{id=%d, price=%s, stock=%s}", id, price, stock);
    }
}
