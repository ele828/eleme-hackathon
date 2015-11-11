package io.github.yfwz100.eleme.hack2015.services;

import io.github.yfwz100.eleme.hack2015.models.Food;

import java.util.List;

/**
 * The foods Service.
 *
 * @author yfwz100
 */
public interface FoodsService {

    /**
     * Query avaliable foods.
     *
     * @return the list of foods.
     */
    List<Food> queryAvailableFoods();
}
