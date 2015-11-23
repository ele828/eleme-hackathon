package io.github.yfwz100.eleme.hack2015;

import io.github.yfwz100.eleme.hack2015.models.Food;
import io.github.yfwz100.eleme.hack2015.services.ContextService;
import io.github.yfwz100.eleme.hack2015.services.FoodsService;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Query the foods.
 *
 * @author yfwz100
 */
public class FoodsServlet extends HttpServlet {

    private final FoodsService foodsService = ContextService.getFoodsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonArrayBuilder foods = Json.createArrayBuilder();
        for (Food food : foodsService.queryAvailableFoods()) {
            foods.add(
                    Json.createObjectBuilder()
                            .add("id", food.getId())
                            .add("price", food.getPrice())
                            .add("stock", food.getStock())
                            .build()
            );
        }
        resp.setCharacterEncoding("utf-8");
        resp.getOutputStream().println(foods.build().toString());
    }
}
