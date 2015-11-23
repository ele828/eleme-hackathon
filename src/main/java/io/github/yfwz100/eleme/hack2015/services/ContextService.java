package io.github.yfwz100.eleme.hack2015.services;

import io.github.yfwz100.eleme.hack2015.database.RedisCache;
import io.github.yfwz100.eleme.hack2015.services.memory.AccessTokenServiceImpl;
import io.github.yfwz100.eleme.hack2015.services.memory.CartsServiceImpl;
import io.github.yfwz100.eleme.hack2015.services.memory.FoodsServiceImpl;
import io.github.yfwz100.eleme.hack2015.services.memory.OrdersServiceImpl;

/**
 * Get the service implementation.
 *
 * @author yfwz100
 */
public class ContextService {

    private static final Cache cache = new RedisCache();
    private static final AccessTokenService accessTokenService = new AccessTokenServiceImpl();
    private static final FoodsService foodsService = new FoodsServiceImpl();
    private static final CartsService cartsService = new CartsServiceImpl();
    private static final OrdersService ordersService = new OrdersServiceImpl();

    public static Cache getCache() {
        return cache;
    }

    public static AccessTokenService getAccessTokenService() {
        return accessTokenService;
    }

    public static CartsService getCartsService() {
        return cartsService;
    }

    public static FoodsService getFoodsService() {
        return foodsService;
    }

    public static OrdersService getOrdersService() {
        return ordersService;
    }
}
