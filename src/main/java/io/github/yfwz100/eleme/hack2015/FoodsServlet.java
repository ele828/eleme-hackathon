package io.github.yfwz100.eleme.hack2015;

import io.github.yfwz100.eleme.hack2015.models.Food;
import io.github.yfwz100.eleme.hack2015.services.AccessTokenService;
import io.github.yfwz100.eleme.hack2015.services.FoodsService;
import io.github.yfwz100.eleme.hack2015.services.mock.FoodsServiceMock;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Query the foods.
 *
 * @author yfwz100
 */
@WebServlet(urlPatterns = "/foods")
public class FoodsServlet extends HttpServlet {

    // FIXME: 15/11/12 Implement the foods service.
    private FoodsService foodsService = new FoodsServiceMock();

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
        resp.getOutputStream().println(foods.build().toString());
    }
}
