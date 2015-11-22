package io.github.yfwz100.eleme.hack2015.database;

import io.github.yfwz100.eleme.hack2015.models.*;
import io.github.yfwz100.eleme.hack2015.util.Props;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The cache implemented in Redis.
 *
 * @author yfwz100
 */
public class RedisCache extends MemoryBasedCache {

    private static final JedisPool pool;

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
        config.setMaxIdle(5);
        //表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
        config.setMaxWaitMillis(1000 * 100);
        //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
        config.setTestOnBorrow(true);

        pool = new JedisPool(config, Props.REDIS_HOST, Props.APP_PORT);
    }

    @Override
    public void addSession(Session session) {
        try (Jedis jedis = pool.getResource()) {
            jedis.set(session.getAccessToken(), session.getUser().getName());
        }
        super.addSession(session);
    }

    @Override
    public Session getSession(String accessToken) {
        Session session = super.getSession(accessToken);
        if (session == null) {
            try (Jedis jedis = pool.getResource()) {
                String actk = jedis.get(accessToken);
                User user = getUser(actk);
                if (user != null) {
                    session = new Session(user, actk);

                }
            }
        }
        return session;
    }

    @Override
    public void addCart(Cart cart) {
        try (Jedis jedis = pool.getResource()) {
            String prefix = "cart:" + cart.getCartId();
            jedis.set(prefix + ":actk", cart.getSession().getAccessToken());
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
                cart = new Cart(getSession(actk));
                for (String key : jedis.keys(prefix + ":item*")) {
                    int val = Integer.parseInt(jedis.get(key));
                    cart.addFood(getFood(Integer.parseInt(key.substring(prefix.length() + 6))), val);
                }
                super.addCart(cart);
            }
        }
        return cart;
    }

    @Override
    public void addOrder(Order order) {
        try (Jedis jedis = pool.getResource()) {
            String prefix = "order:" + order.getOrderId();
            jedis.set(prefix + ":user", order.getUser().getName());
            for (Map.Entry<Integer, Integer> e : order.getMenu().entrySet()) {
                jedis.set(prefix + ":item:" + e.getKey(), e.getValue().toString());
            }
        }
        super.addOrder(order);
    }

    @Override
    public Order getOrder(String oId) {
        Order order = super.getOrder(oId);
        if (order == null) {
            String prefix = "order:" + oId;
            try (Jedis jedis = pool.getResource()) {
                User user = getUser(jedis.get(prefix + ":user"));
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
            }
        }
        return order;
    }

    @Override
    public Collection<Food> getFoods() {
        // FIXME: inconsistent across machines.
        return super.getFoods();
    }

    @Override
    public Food getFood(int foodId) {
        // FIXME: inconsistent across machines.
        return super.getFood(foodId);
    }
}
