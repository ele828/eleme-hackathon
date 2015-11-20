package io.github.yfwz100.eleme.hack2015.services;

import io.github.yfwz100.eleme.hack2015.database.DatabasePool;
import io.github.yfwz100.eleme.hack2015.models.Food;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Foods service.
 *
 * @author yfwz100
 */
public class FoodsService {
    public List<Food> queryAvailableFoods() {
        List<Food> foods = new ArrayList<>();

        try (
                Connection conn = DatabasePool.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("select * from food;");
        ) {
            while (rs.next()) {
                int id = rs.getInt("id");
                int stock = rs.getInt("stock");
                double price = rs.getDouble("price");
                foods.add(new Food(id, price, stock));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return foods;
    }

    public Food getFood(int foodId) {
        final String sql = "select * from food where id=?";
        Food food = null;

        try (
                Connection conn = DatabasePool.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            pstmt.setInt(1, foodId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    int stock = rs.getInt("stock");
                    double price = rs.getDouble("price");
                    food = new Food(id, price, stock);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return food;
    }

}
