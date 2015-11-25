package io.github.yfwz100.eleme.hack2015.database;

import io.github.yfwz100.eleme.hack2015.models.*;
import io.github.yfwz100.eleme.hack2015.services.Cache;
import io.github.yfwz100.eleme.hack2015.services.exceptions.FoodOutOfStockException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The cache layer.
 *
 * @author eric
 */
public class MemoryPoolCache implements Cache {
    protected final Map<String, Session> sessionPool = new HashMap<>();
    protected final Map<String, Cart> cartPool = new HashMap<>();
    protected final Map<String, Order> orderPool = new HashMap<>();
    protected final Map<String, User> userPool = new HashMap<>(1000);
    protected final Map<Integer, Food> foodPool = new HashMap<>(1000);

    {
        init();
    }

    protected void init() {
        poolUsers();
        poolFoods();
    }

    protected void poolUsers() {
        try (
                Connection conn = DatabasePool.getConnection();
                Statement stat = conn.createStatement();
                ResultSet resultSet = stat.executeQuery("select * from user")
        ) {
            while (resultSet.next()) {
                userPool.put(
                        resultSet.getString("name"),
                        new User(
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getString("password")
                        )
                );
            }
        } catch (SQLException ignored) {
        }
    }

    protected void poolFoods() {
        try (
                Connection conn = DatabasePool.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("select * from food")
        ) {
            while (rs.next()) {
                int id = rs.getInt("id");
                int stock = rs.getInt("stock");
                int price = rs.getInt("price");
                foodPool.put(id, new Food(id, price, stock));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void addSession(Session session) {
        sessionPool.put(session.getAccessToken(), session);
    }

    @Override
    public Session getSession(String accessToken) {
        return sessionPool.get(accessToken);
    }

    @Override
    public void addCart(Cart cart) {
        cartPool.put(cart.getCartId(), cart);
    }

    @Override
    public Cart getCart(String cid) {
        return cartPool.get(cid);
    }

    @Override
    public void addOrder(Order order) {
        orderPool.put(order.getOrderId(), order);
    }

    @Override
    public Order getOrder(String oId) {
        return orderPool.get(oId);
    }

    @Override
    public Collection<Order> getOrders() {
        return orderPool.values();
    }

    @Override
    public User getUser(String name) {
        return userPool.get(name);
    }

    @Override
    public Food getFood(int foodId) {
        return foodPool.get(foodId);
    }

    @Override
    public Collection<Food> getFoods() {
        return foodPool.values();
    }

    @Override
    public int consumeFood(int id, int quantity) throws FoodOutOfStockException {
        throw new FoodOutOfStockException();
    }
}
