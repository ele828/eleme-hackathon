package io.github.yfwz100.eleme.hack2015;

import io.github.yfwz100.eleme.hack2015.exceptions.CartNotFoundException;
import io.github.yfwz100.eleme.hack2015.exceptions.FoodOutOfStockException;
import io.github.yfwz100.eleme.hack2015.exceptions.NoAccessToCartException;
import io.github.yfwz100.eleme.hack2015.exceptions.OrderOutOfLimitException;
import io.github.yfwz100.eleme.hack2015.models.Food;
import io.github.yfwz100.eleme.hack2015.models.Order;
import io.github.yfwz100.eleme.hack2015.models.User;
import io.github.yfwz100.eleme.hack2015.services.CartsService;
import io.github.yfwz100.eleme.hack2015.services.OrdersService;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by Eric on 15/11/12.
 */
public class OrderServlet extends HttpServlet {

    private OrdersService orderService = new OrdersService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String accessToken = req.getAttribute(AccessTokenFilter.ACCESSTOKEN).toString();

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
            resp.getOutputStream().println(
                    Json.createObjectBuilder()
                            .add("code", "ORDER_OUT_OF_LIMIT")
                            .add("message", "每个用户只能下一单")
                            .build()
                            .toString()
            );
        } catch (CartNotFoundException e) {
            resp.setStatus(404);
            resp.setCharacterEncoding("utf-8");
            resp.getOutputStream().println(
                    Json.createObjectBuilder()
                            .add("code", "CART_NOT_FOUND")
                            .add("message", "篮子不存在")
                            .build()
                            .toString()
            );
        } catch (NoAccessToCartException e) {
            resp.setStatus(403);
            resp.setCharacterEncoding("utf-8");
            resp.getOutputStream().println(
                    Json.createObjectBuilder()
                            .add("code", "NOT_AUTHORIZED_TO_ACCESS_CART")
                            .add("message", "无权限访问指定的篮子")
                            .build()
                            .toString()
            );
        } catch (FoodOutOfStockException e) {
            resp.setStatus(403);
            resp.setCharacterEncoding("utf-8");
            resp.getOutputStream().println(
                    Json.createObjectBuilder()
                            .add("code", "FOOD_OUT_OF_STOCK")
                            .add("message", "食物库存不足")
                            .build()
                            .toString()
            );
        } catch(SQLException e) {
            resp.setStatus(400);
            resp.getOutputStream().println(
                    Json.createObjectBuilder()
                            .add("code", "DATABASE_ERROR")
                            .add("message", "数据库错误" + e.getMessage())
                            .build()
                            .toString()
            );
        } catch (Exception e) {
            resp.setStatus(400);
            System.out.println("格式错误: " + e.getMessage());
            resp.getOutputStream().println(
                    Json.createObjectBuilder()
                            .add("code", "MALFORMED_JSON")
                            .add("message", "格式错误")
                            .build()
                            .toString()
            );
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String accessToken = req.getAttribute(AccessTokenFilter.ACCESSTOKEN).toString();

        JsonArrayBuilder jOrders = Json.createArrayBuilder();
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        Order order = orderService.getOrder(accessToken);

        double total = 0;
        if (order == null) {
            resp.setStatus(200);
            resp.setCharacterEncoding("utf-8");
            resp.getOutputStream().println(
                    jOrders.build().toString()
            );
            return;
        }

        for (Food food : order.getItems()) {
            total += food.getCount() * food.getPrice();
            jsonArrayBuilder.add(
                    Json.createObjectBuilder()
                            .add("food_id", food.getId())
                            .add("count", food.getCount())
                            .build()
            );
        }
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
