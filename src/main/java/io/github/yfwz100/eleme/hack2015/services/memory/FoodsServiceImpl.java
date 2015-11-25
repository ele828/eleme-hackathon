package io.github.yfwz100.eleme.hack2015.services.memory;

import io.github.yfwz100.eleme.hack2015.models.Food;
import io.github.yfwz100.eleme.hack2015.services.Cache;
import io.github.yfwz100.eleme.hack2015.services.ContextService;
import io.github.yfwz100.eleme.hack2015.services.FoodsService;
import io.github.yfwz100.eleme.hack2015.services.exceptions.FoodOutOfStockException;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Foods service.
 *
 * @author eric
 * @author yfwz100
 */
public class FoodsServiceImpl implements FoodsService {

    private final Cache cache = ContextService.getCache();
    private final Timer foodsTimer = new Timer(true);
    private transient Collection<Food> foods;

    {
        foodsTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                foods = cache.getFoods();
            }
        }, 0, 10);
    }

    @Override
    public Collection<Food> queryAvailableFoods() {
        return foods;
    }

    @Override
    public Food getFood(int foodId) {
        return cache.getFood(foodId);
    }

    @Override
    public int consumeFood(int foodId, int quantity) throws FoodOutOfStockException {
        int val = cache.consumeFood(foodId, quantity);
        if (val < 0) {
            throw new FoodOutOfStockException();
        }
        return val;
    }

}
