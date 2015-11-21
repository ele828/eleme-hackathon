package io.github.yfwz100.eleme.hack2015.services;

import io.github.yfwz100.eleme.hack2015.models.Food;
import io.github.yfwz100.eleme.hack2015.services.exceptions.FoodOutOfStockException;

import java.util.Collection;

/**
 * The service interface for foods.
 *
 * @author yfwz100
 */
public interface FoodsService {

    /**
     * Query the available foods.
     *
     * @return the collection of food.
     */
    Collection<Food> queryAvailableFoods();

    /**
     * Get food by id.
     *
     * @param foodId the id of food.
     * @return the food.
     */
    Food getFood(int foodId);

    /**
     * Consume the food.
     *
     * @param foodId   the id of food.
     * @param quantity the quantity.
     * @return the quantity actually consumed.
     */
    int consumeFood(int foodId, int quantity) throws FoodOutOfStockException;
}
