package io.github.yfwz100.eleme.hack2015.database;

import io.github.yfwz100.eleme.hack2015.models.*;
import io.github.yfwz100.eleme.hack2015.services.Cache;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The cache layer.
 *
 * @author eric
 */
public class MemoryBasedCache implements Cache {
    protected final Map<String, Session> sessionPool = new ConcurrentHashMap<>();
    protected final Map<String, Cart> cartPool = new ConcurrentHashMap<>();
    protected final Map<String, Order> orderPool = new ConcurrentHashMap<>();
    private static final Map<String, User> userPool = new HashMap<>(1000);
    private static final Map<Integer, Food> foodPool = new HashMap<>(1000);

    static {
        poolUsers();
        poolFoods();
    }

    private static void poolUsers() {
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

    private static void poolFoods() {
        try (
                Connection conn = DatabasePool.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("select * from food")
        ) {
            while (rs.next()) {
                int id = rs.getInt("id");
                int stock = rs.getInt("stock");
                double price = rs.getDouble("price");
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
}
