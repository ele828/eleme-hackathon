package io.github.yfwz100.eleme.hack2015.services;

import io.github.yfwz100.eleme.hack2015.DatabasePool;
import io.github.yfwz100.eleme.hack2015.models.Food;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Foods service.
 *
 * @author yfwz100
 */
public class FoodsService {
    public List<Food> queryAvailableFoods() {
        List<Food> foods = new ArrayList<>();

        DatabasePool pool = DatabasePool.getInstance();
        Connection conn = null;
        try {
            conn = pool.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from food;");
            while (rs.next()) {
                int id = rs.getInt("id");
                int stock = rs.getInt("stock");
                double price = rs.getDouble("price");
                foods.add(new Food(id, price, stock));
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return foods;
    }

    public Food getFood(int foodId) {
        DatabasePool pool = DatabasePool.getInstance();
        Connection conn = null;
        Food food = null;
        try {
            conn = pool.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            String sql = "select * from food where id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, foodId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int stock = rs.getInt("stock");
                double price = rs.getDouble("price");
                food = new Food(id, price, stock);
            }
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return food;
    }

}
