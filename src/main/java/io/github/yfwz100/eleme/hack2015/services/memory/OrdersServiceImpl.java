package io.github.yfwz100.eleme.hack2015.services.memory;

import io.github.yfwz100.eleme.hack2015.models.Cart;
import io.github.yfwz100.eleme.hack2015.models.Order;
import io.github.yfwz100.eleme.hack2015.models.Session;
import io.github.yfwz100.eleme.hack2015.services.Cache;
import io.github.yfwz100.eleme.hack2015.services.ContextService;
import io.github.yfwz100.eleme.hack2015.services.FoodsService;
import io.github.yfwz100.eleme.hack2015.services.OrdersService;
import io.github.yfwz100.eleme.hack2015.services.exceptions.CartNotFoundException;
import io.github.yfwz100.eleme.hack2015.services.exceptions.FoodOutOfStockException;
import io.github.yfwz100.eleme.hack2015.services.exceptions.NoAccessToCartException;
import io.github.yfwz100.eleme.hack2015.services.exceptions.OrderOutOfLimitException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The services for orders.
 *
 * @author eric
 * @author yfwz100
 */
public class OrdersServiceImpl implements OrdersService {

    private final Cache cache = ContextService.getCache();
    private final FoodsService foodsService = ContextService.getFoodsService();

    @Override
    public Order generateOrder(String accessToken, String cartId)
            throws CartNotFoundException, NoAccessToCartException, FoodOutOfStockException, OrderOutOfLimitException {
        Cart cart = cache.getCart(cartId);

        // check validation of cart.
        if (cart == null)
            throw new CartNotFoundException();

        // check validation of session.
        if (!cart.getSession().getAccessToken().equals(accessToken))
            throw new NoAccessToCartException();

        // check cart limitation of session.
        if (cart.getSession().getUser().getOrder() != null)
            throw new OrderOutOfLimitException();

        Map<Integer, Integer> menu = new HashMap<>();
        for (Map.Entry<Integer, AtomicInteger> entry : cart.getMenu()) {
            foodsService.consumeFood(entry.getKey(), entry.getValue().get());
            menu.put(entry.getKey(), entry.getValue().get());
        }

        Order order = new Order(cart.getSession().getUser(), menu);
        cart.getSession().getUser().setOrder(order);
        cache.addOrder(order);

        return order;
    }

    @Override
    public Order getOrder(String accessToken) {
        Session session = cache.getSession(accessToken);
        return session == null ? null : session.getUser().getOrder();
    }

    @Override
    public Collection<Order> getOrders() {
        return cache.getOrders();
    }
}
