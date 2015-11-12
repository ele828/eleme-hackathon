package io.github.yfwz100.eleme.hack2015.services;

import io.github.yfwz100.eleme.hack2015.Storage;
import io.github.yfwz100.eleme.hack2015.exceptions.CartNotFoundException;
import io.github.yfwz100.eleme.hack2015.exceptions.FoodNotFoundException;
import io.github.yfwz100.eleme.hack2015.exceptions.FoodOutOfLimitException;
import io.github.yfwz100.eleme.hack2015.exceptions.NoAccessToCartException;
import io.github.yfwz100.eleme.hack2015.models.Cart;
import io.github.yfwz100.eleme.hack2015.models.Food;
import io.github.yfwz100.eleme.hack2015.models.User;

/**
 * The mock of carts service.
 *
 * @author yfwz100
 */
public class CartsService {

    FoodsService foodsService = new FoodsService();
    public Cart createCart(String accessToken) {
        User user = Storage.getUser(accessToken);
        Cart cart = new Cart(user);
        Storage.addCart(cart);
        return cart;
    }

    public void addFoodToCart(String accessToken, String cartId, int foodId, int count)
            throws FoodOutOfLimitException, FoodNotFoundException, CartNotFoundException, NoAccessToCartException {

        Cart cart = Storage.getCart(cartId);
        if (cart == null)
            throw new CartNotFoundException();

        if (!cart.getUser().getAccessToken().equals(accessToken))
            throw new NoAccessToCartException();

        Food food = foodsService.getFood(foodId);
        if (food == null)
            throw new FoodNotFoundException();

        if (!cart.checkAvailable(count))
            throw new FoodOutOfLimitException();

        for (int i = 0; i < count; i++) {
            boolean success = cart.addFood(food);
            if (!success)
                throw new FoodOutOfLimitException();
        }
    }

    public void generateOrder(String cartId)  {
    }
}
