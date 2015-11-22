package io.github.yfwz100.eleme.hack2015.services.memory;

import io.github.yfwz100.eleme.hack2015.models.Food;
import io.github.yfwz100.eleme.hack2015.services.Cache;
import io.github.yfwz100.eleme.hack2015.services.ContextService;
import io.github.yfwz100.eleme.hack2015.services.FoodsService;
import io.github.yfwz100.eleme.hack2015.services.exceptions.FoodOutOfStockException;

import java.util.Collection;

/**
 * Foods service.
 *
 * @author eric
 * @author yfwz100
 */
public class FoodsServiceImpl implements FoodsService {

    private static final Cache cache = ContextService.getCache();

    @Override
    public Collection<Food> queryAvailableFoods() {
        return cache.getFoods();
    }

    @Override
    public Food getFood(int foodId) {
        return cache.getFood(foodId);
    }

    // TODO: replace synchronized keyword!
    @Override
    public synchronized int consumeFood(int foodId, int quantity) throws FoodOutOfStockException {
        Food food = cache.getFood(foodId);
        if (food.getStock() - quantity >= 0) {
            return food.consumeStock(quantity);
        } else {
            throw new FoodOutOfStockException();
        }
    }

}
