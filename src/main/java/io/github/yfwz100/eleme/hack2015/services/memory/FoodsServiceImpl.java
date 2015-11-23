package io.github.yfwz100.eleme.hack2015.services.memory;

import io.github.yfwz100.eleme.hack2015.models.Food;
import io.github.yfwz100.eleme.hack2015.services.Cache;
import io.github.yfwz100.eleme.hack2015.services.ContextService;
import io.github.yfwz100.eleme.hack2015.services.FoodsService;
import io.github.yfwz100.eleme.hack2015.services.exceptions.FoodOutOfStockException;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * Foods service.
 *
 * @author eric
 * @author yfwz100
 */
public class FoodsServiceImpl implements FoodsService {

    private final Cache cache = ContextService.getCache();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
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
        Future<Integer> result = executorService.submit(() -> {
            Food food = cache.getFood(foodId);
            if (food.getStock() - quantity >= 0) {
                return food.consumeStock(quantity);
            } else {
                return -1;
            }
        });
        try {
            int val = result.get(1, TimeUnit.SECONDS);
            if (val < 0) {
                throw new FoodOutOfStockException();
            } else {
                return val;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new FoodOutOfStockException();
        }
    }

}
