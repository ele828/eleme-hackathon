package io.github.yfwz100.eleme.hack2015.services.memory;

import io.github.yfwz100.eleme.hack2015.database.Cache;
import io.github.yfwz100.eleme.hack2015.services.exceptions.*;
import io.github.yfwz100.eleme.hack2015.models.Cart;
import io.github.yfwz100.eleme.hack2015.models.Food;
import io.github.yfwz100.eleme.hack2015.models.Session;
import io.github.yfwz100.eleme.hack2015.services.CartsService;
import io.github.yfwz100.eleme.hack2015.services.FoodsService;

/**
 * The mock of carts service.
 *
 * @author yfwz100
 */
public class CartsServiceImpl implements CartsService {

    private static final int MAX_FOOD_SIZE = 3;

    FoodsService foodsService = FoodsServiceImpl.getInstance();

    @Override
    public Cart createCart(String accessToken) {
        Session session = Cache.getSession(accessToken);
        Cart cart = new Cart(session);
        Cache.addCart(cart);
        return cart;
    }

    @Override
    public void addFoodToCart(String accessToken, String cartId, int foodId, int count)
            throws FoodOutOfLimitException, FoodNotFoundException, CartNotFoundException, NoAccessToCartException {
        Cart cart = Cache.getCart(cartId);
        if (cart == null)
            throw new CartNotFoundException();

        if (!cart.getSession().getAccessToken().equals(accessToken))
            throw new NoAccessToCartException();

        Food food = foodsService.getFood(foodId);

        if (food == null)
            throw new FoodNotFoundException();

        if (cart.getSize() + count > MAX_FOOD_SIZE)
            throw new FoodOutOfLimitException();

        cart.addFood(food, count);
    }

    private static final CartsService cartsService = new CartsServiceImpl();

    private CartsServiceImpl() {
    }

    public static CartsService getInstance() {
        return cartsService;
    }
}
