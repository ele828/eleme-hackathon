package io.github.yfwz100.eleme.hack2015.services.memory;

import io.github.yfwz100.eleme.hack2015.database.Cache;
import io.github.yfwz100.eleme.hack2015.models.Cart;
import io.github.yfwz100.eleme.hack2015.models.Order;
import io.github.yfwz100.eleme.hack2015.models.Session;
import io.github.yfwz100.eleme.hack2015.services.FoodsService;
import io.github.yfwz100.eleme.hack2015.services.OrdersService;
import io.github.yfwz100.eleme.hack2015.services.exceptions.CartNotFoundException;
import io.github.yfwz100.eleme.hack2015.services.exceptions.FoodOutOfStockException;
import io.github.yfwz100.eleme.hack2015.services.exceptions.NoAccessToCartException;
import io.github.yfwz100.eleme.hack2015.services.exceptions.OrderOutOfLimitException;

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

    FoodsService foodsService = FoodsServiceImpl.getInstance();

    @Override
    public Order generateOrder(String accessToken, String cartId)
            throws CartNotFoundException, NoAccessToCartException, FoodOutOfStockException, OrderOutOfLimitException {
        Cart cart = Cache.getCart(cartId);

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

        return order;
    }

    @Override
    public Order getOrder(String accessToken) {
        Session session = Cache.getSession(accessToken);
        return session == null ? null : session.getUser().getOrder();
    }

    // -- singleton pattern

    private static final OrdersService orderService = new OrdersServiceImpl();

    private OrdersServiceImpl() {
    }

    public static OrdersService getInstance() {
        return orderService;
    }

}
