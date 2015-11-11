package io.github.yfwz100.eleme.hack2015.services.mock;

import io.github.yfwz100.eleme.hack2015.models.Food;
import io.github.yfwz100.eleme.hack2015.services.FoodsService;

import java.util.Collections;
import java.util.List;

/**
 * The mock of foods service.
 *
 * @author yfwz100
 */
public class FoodsServiceMock implements FoodsService {
    @Override
    public List<Food> queryAvailableFoods() {
        return Collections.emptyList();
    }
}
