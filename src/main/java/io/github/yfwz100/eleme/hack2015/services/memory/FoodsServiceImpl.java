package io.github.yfwz100.eleme.hack2015.services.memory;

import io.github.yfwz100.eleme.hack2015.database.DatabasePool;
import io.github.yfwz100.eleme.hack2015.models.Food;
import io.github.yfwz100.eleme.hack2015.services.FoodsService;
import io.github.yfwz100.eleme.hack2015.services.exceptions.FoodOutOfStockException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Foods service.
 *
 * @author eric
 * @author yfwz100
 */
public class FoodsServiceImpl implements FoodsService {

    private static final Map<Integer, Food> foods = new TreeMap<>();

    static {
        try (
                Connection conn = DatabasePool.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("select * from food")
        ) {
            while (rs.next()) {
                int id = rs.getInt("id");
                int stock = rs.getInt("stock");
                double price = rs.getDouble("price");
                foods.put(id, new Food(id, price, stock));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<Food> queryAvailableFoods() {
        return foods.values();
    }

    @Override
    public Food getFood(int foodId) {
        return foods.get(foodId);
    }

    // TODO: replace synchronized keyword!
    @Override
    public synchronized int consumeFood(int foodId, int quantity) throws FoodOutOfStockException {
        Food food = foods.get(foodId);
        if (food.getStock() - quantity >= 0) {
            return food.consumeStock(quantity);
        } else {
            throw new FoodOutOfStockException();
        }
    }

    private static final FoodsService foodsService = new FoodsServiceImpl();

    private FoodsServiceImpl() {
    }

    public static FoodsService getInstance() {
        return foodsService;
    }
}
