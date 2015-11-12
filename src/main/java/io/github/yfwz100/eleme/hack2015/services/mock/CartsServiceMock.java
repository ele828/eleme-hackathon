package io.github.yfwz100.eleme.hack2015.services.mock;

import io.github.yfwz100.eleme.hack2015.services.CartsService;

/**
 * The mock of carts service.
 *
 * @author yfwz100
 */
public class CartsServiceMock implements CartsService {
    @Override
    public String getCartId(String accessToken) {
        return "11.11";
    }

    @Override
    public void addFood(int foodId, int count) throws CartsServiceException {
    }

    @Override
    public void generateOrder(String cartId) throws CartsServiceException {
    }
}
