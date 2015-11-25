package io.github.yfwz100.eleme.hack2015.database;

import io.github.yfwz100.eleme.hack2015.models.*;
import io.github.yfwz100.eleme.hack2015.services.exceptions.FoodOutOfStockException;
import io.github.yfwz100.eleme.hack2015.util.Props;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The cache implemented in Redis.
 *
 * @author yfwz100
 */
public class RedisCache extends MemoryPoolCache {

    private JedisPool pool;
    private final String consumeFoodScript;

    private final Set<String> orderIds = new HashSet<>();

    {
        try (BufferedReader inputStream =
                     new BufferedReader(
                             new InputStreamReader(
                                     RedisCache.class.getResourceAsStream("/consume_food.lua")
                             )
                     )
        ) {
            StringJoiner script = new StringJoiner("\n");
            String line;
            while ((line = inputStream.readLine()) != null) {
                script.add(line);
            }
            consumeFoodScript = script.toString();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Override
    protected void init() {
        JedisPoolConfig config = new JedisPoolConfig();
        //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
        config.setMaxIdle(5);
        //表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
        config.setMaxWaitMillis(1000 * 100);
        //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
        config.setTestOnBorrow(true);
        //最大实例数.
        config.setMaxTotal(100);

        pool = new JedisPool(config, Props.REDIS_HOST, Props.REDIS_PORT);

        try (Jedis jedis = pool.getResource()) {
            jedis.del("orders");
        }

        super.init();
    }

    private class RedisFood extends Food {

        private final String key;

        public RedisFood(int id, int price, int stock) {
            super(id, price, stock);
            this.key = "food:" + getId();
            try (Jedis jedis = pool.getResource()) {
                jedis.set(key, String.valueOf(stock));
            }
        }

        @Override
        public int consumeStock(int quantity) {
            try (Jedis jedis = pool.getResource()) {
                long val = jedis.decrBy(key, quantity);
                return (int) val;
            }
        }

        @Override
        public int getStock() {
            try (Jedis jedis = pool.getResource()) {
                return Integer.parseInt(jedis.get("food:" + getId()));
            }
        }
    }

    @Override
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
                foodPool.put(id, new RedisFood(id, price, stock));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addSession(Session session) {
        final String key = "user:" + session.getAccessToken();
        try (Jedis jedis = pool.getResource()) {
            jedis.set(key, session.getUser().getName());
        }
        super.addSession(session);
    }

    @Override
    public Session getSession(String accessToken) {
        if (accessToken != null) {
            Session session = super.getSession(accessToken);
            if (session == null) {
                try (Jedis jedis = pool.getResource()) {
                    final String key = "user:" + accessToken;
                    String name = jedis.get(key);
                    if (name != null) {
                        User user = getUser(name);
                        if (user != null) {
                            session = new Session(user, name);

                        }
                    } else {
                        return null;
                    }
                }
            }
            return session;
        } else {
            return null;
        }
    }

    @Override
    public void addCart(Cart cart) {
        try (Jedis jedis = pool.getResource()) {
            String prefix = "cart:" + cart.getCartId();
            jedis.set(prefix + ":actk", cart.getSession().getAccessToken());
            jedis.set(prefix + ":user", cart.getSession().getUser().getName());
            for (Map.Entry<Integer, AtomicInteger> e : cart.getMenu()) {
                jedis.set(prefix + ":item:" + e.getKey(), e.getValue().toString());
            }
        }
        super.addCart(cart);
    }

    @Override
    public Cart getCart(String cid) {
        Cart cart = super.getCart(cid);
        if (cart == null) {
            try (Jedis jedis = pool.getResource()) {
                String prefix = "cart:" + cid;
                String actk = jedis.get(prefix + ":actk");
                if (actk != null) {
                    String user = jedis.get(prefix + ":user");
                    cart = new Cart(new Session(getUser(user), actk));
                    for (String key : jedis.keys(prefix + ":item*")) {
                        int val = Integer.parseInt(jedis.get(key));
                        cart.addFood(getFood(Integer.parseInt(key.substring(prefix.length() + 6))), val);
                    }
                    super.addCart(cart);
                } else {
                    return null;
                }
            }
        }
        return cart;
    }

    @Override
    public void addOrder(Order order) {
        if (order.getOrderId() != null) {
            try (Jedis jedis = pool.getResource()) {
                String prefix = "order:" + order.getOrderId();
                jedis.sadd("orders", order.getOrderId());
                orderIds.add(order.getOrderId());
                jedis.set(prefix + ":user", order.getUser().getName());
                for (Map.Entry<Integer, Integer> e : order.getMenu().entrySet()) {
                    jedis.set(prefix + ":item:" + e.getKey(), e.getValue().toString());
                }
            }
            super.addOrder(order);
        }
    }

    @Override
    public Order getOrder(String oId) {
        Order order = super.getOrder(oId);
        if (order == null) {
            String prefix = "order:" + oId;
            try (Jedis jedis = pool.getResource()) {
                String name = jedis.get(prefix + ":user");
                if (name != null) {
                    User user = getUser(name);
                    if (user != null) {
                        order = user.getOrder();
                        if (order == null) {
                            HashMap<Integer, Integer> menu = new HashMap<>(3);
                            for (String key : jedis.keys(prefix + ":item*")) {
                                int val = Integer.parseInt(jedis.get(key));
                                menu.put(Integer.parseInt(key.substring(prefix.length() + 6)), val);
                            }
                            order = new Order(user, menu);
                            super.addOrder(order);
                        }
                    }
                } else {
                    return null;
                }
            }
        }
        return order;
    }

    @Override
    public int consumeFood(int id, int quantity) throws FoodOutOfStockException {
        try (Jedis jedis = pool.getResource()) {
            long val = (Long) jedis.eval(consumeFoodScript, 1, String.valueOf(id), String.valueOf(quantity));
            return (int) val;
        }
    }

    private static final String[] zeros = new String[0];

    @Override
    public Collection<Order> getOrders() {
        try (Jedis jedis = pool.getResource()) {
            Set<String> orders = jedis.smembers("orders");
            orders.removeAll(orderPool.keySet());
            if (!orders.isEmpty()) {
                for (String orderId : orders) {
                    getOrder(orderId);
                }
            }
        }
        return super.getOrders();
    }
}
