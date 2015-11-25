package io.github.yfwz100.eleme.hack2015;

import io.github.yfwz100.eleme.hack2015.models.Order;
import io.github.yfwz100.eleme.hack2015.services.ContextService;
import io.github.yfwz100.eleme.hack2015.services.FoodsService;
import io.github.yfwz100.eleme.hack2015.services.OrdersService;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The admin orders servlet.
 *
 * @author yfwz100
 */
public class AdminOrdersServlet extends HttpServlet {

    private final OrdersService ordersService = ContextService.getOrdersService();
    private final FoodsService foodsService = ContextService.getFoodsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonArrayBuilder jOrdersBuilder = Json.createArrayBuilder();
        for (Order order : ordersService.getOrders()) {
            JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

            double total = order.getMenu().entrySet().stream().mapToDouble(e -> {
                jsonArrayBuilder.add(
                        Json.createObjectBuilder().add("food_id", e.getKey()).add("count", e.getValue()).build()
                );
                return foodsService.getFood(e.getKey()).getPrice() * e.getValue();
            }).sum();

            jOrdersBuilder.add(
                    Json.createObjectBuilder()
                            .add("id", order.getOrderId())
                            .add("user_id", order.getUser().getId())
                            .add("items", jsonArrayBuilder)
                            .add("total", total)
                            .build()
            );
        }
        resp.setCharacterEncoding("utf-8");
        resp.getOutputStream().println(jOrdersBuilder.build().toString());
        System.out.println(jOrdersBuilder.build().toString());
    }
}
