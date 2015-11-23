package io.github.yfwz100.eleme.hack2015;

import io.github.yfwz100.eleme.hack2015.models.Order;
import io.github.yfwz100.eleme.hack2015.services.ContextService;
import io.github.yfwz100.eleme.hack2015.services.FoodsService;
import io.github.yfwz100.eleme.hack2015.services.OrdersService;
import io.github.yfwz100.eleme.hack2015.services.exceptions.CartNotFoundException;
import io.github.yfwz100.eleme.hack2015.services.exceptions.FoodOutOfStockException;
import io.github.yfwz100.eleme.hack2015.services.exceptions.NoAccessToCartException;
import io.github.yfwz100.eleme.hack2015.services.exceptions.OrderOutOfLimitException;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The order servlet.
 *
 * @author eric
 */
public class OrderServlet extends HttpServlet {

    private static final String ORDER_OUT_OF_LIMIT_JSON = Json.createObjectBuilder()
            .add("code", "ORDER_OUT_OF_LIMIT")
            .add("message", "每个用户只能下一单")
            .build()
            .toString();
    private static final String CART_NOT_FOUND_JSON = Json.createObjectBuilder()
            .add("code", "CART_NOT_FOUND")
            .add("message", "篮子不存在")
            .build()
            .toString();
    private static final String NOT_AUTHORIZED_TO_ACCESS_CART_JSON = Json.createObjectBuilder()
            .add("code", "NOT_AUTHORIZED_TO_ACCESS_CART")
            .add("message", "无权限访问指定的篮子")
            .build()
            .toString();
    private static final String FOOD_OUT_OF_STOCK_JSON = Json.createObjectBuilder()
            .add("code", "FOOD_OUT_OF_STOCK")
            .add("message", "食物库存不足")
            .build()
            .toString();
    private static final String MALFORMED_JSON = Json.createObjectBuilder()
            .add("code", "MALFORMED_JSON")
            .add("message", "格式错误")
            .build()
            .toString();

    private final OrdersService orderService = ContextService.getOrdersService();
    private final FoodsService foodsService = ContextService.getFoodsService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String accessToken = req.getAttribute(AccessTokenFilter.ACCESS_TOKEN).toString();

        resp.setCharacterEncoding("utf-8");
        try (JsonReader reader = Json.createReader(req.getInputStream())) {
            JsonObject info = reader.readObject();
            String cartId = info.getString("cart_id");

            Order order = orderService.generateOrder(accessToken, cartId);
            resp.getOutputStream().println(
                    Json.createObjectBuilder()
                            .add("id", order.getOrderId())
                            .build()
                            .toString()
            );
        } catch (OrderOutOfLimitException e) {
            resp.setStatus(403);
            resp.setCharacterEncoding("utf-8");
            resp.getOutputStream().println(ORDER_OUT_OF_LIMIT_JSON);
        } catch (CartNotFoundException e) {
            resp.setStatus(404);
            resp.setCharacterEncoding("utf-8");
            resp.getOutputStream().println(CART_NOT_FOUND_JSON);
        } catch (NoAccessToCartException e) {
            resp.setStatus(403);
            resp.setCharacterEncoding("utf-8");
            resp.getOutputStream().println(NOT_AUTHORIZED_TO_ACCESS_CART_JSON);
        } catch (FoodOutOfStockException e) {
            resp.setStatus(403);
            resp.setCharacterEncoding("utf-8");
            resp.getOutputStream().println(FOOD_OUT_OF_STOCK_JSON);
        } catch (Exception e) {
            resp.setStatus(400);
            resp.getOutputStream().println(MALFORMED_JSON);
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String accessToken = req.getAttribute(AccessTokenFilter.ACCESS_TOKEN).toString();

        Order order = orderService.getOrder(accessToken);

        if (order == null) {
            resp.setStatus(200);
            resp.setCharacterEncoding("utf-8");
            resp.getOutputStream().println("[]");
        } else {
            JsonArrayBuilder jOrders = Json.createArrayBuilder();
            JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

            double total = order.getMenu().entrySet().stream().mapToDouble(e -> {
                jsonArrayBuilder.add(
                        Json.createObjectBuilder().add("food_id", e.getKey()).add("count", e.getValue()).build()
                );
                return foodsService.getFood(e.getKey()).getPrice() * e.getValue();
            }).sum();

            resp.setCharacterEncoding("utf-8");
            jOrders.add(
                    Json.createObjectBuilder()
                            .add("id", order.getOrderId())
                            .add("user_id", order.getUser().getId())
                            .add("items", jsonArrayBuilder)
                            .add("total", total)
                            .build()
            );
            resp.getOutputStream().println(jOrders.build().toString());
        }
    }
}
