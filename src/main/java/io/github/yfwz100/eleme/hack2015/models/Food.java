package io.github.yfwz100.eleme.hack2015.models;

/**
 * Food Model.
 *
 * @author yfwz100
 */
public class Food {

    private int id;
    private double price;
    private int stock;

    public Food() {
    }

    public Food(int id, double price, int stock) {
        this.id = id;
        this.price = price;
        this.stock = stock;
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
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @Override
    public String toString() {
        return "Food{" +
                "id=" + id +
                ", price=" + price +
                ", stock=" + stock +
                '}';
    }
}
