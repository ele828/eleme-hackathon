package io.github.yfwz100.eleme.hack2015;

import io.github.yfwz100.eleme.hack2015.database.Cache;
import io.github.yfwz100.eleme.hack2015.services.exceptions.CartNotFoundException;
import io.github.yfwz100.eleme.hack2015.services.exceptions.FoodNotFoundException;
import io.github.yfwz100.eleme.hack2015.services.exceptions.FoodOutOfLimitException;
import io.github.yfwz100.eleme.hack2015.services.exceptions.NoAccessToCartException;
import io.github.yfwz100.eleme.hack2015.services.CartsService;
import io.github.yfwz100.eleme.hack2015.services.memory.CartsServiceImpl;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * AddFoodServlet.
 *
 * @author eric
 */
public class AddFoodServlet extends HttpServlet {

    CartsService cartsService = CartsServiceImpl.getInstance();

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String accessToken = req.getAttribute(AccessTokenFilter.ACCESS_TOKEN).toString();

        try (JsonReader reader = Json.createReader(req.getInputStream())) {
            JsonObject info = reader.readObject();
            int foodId = info.getInt("food_id");
            int count = info.getInt("count");
//            String cartId = req.getRequestURI().replace("/carts/", "");
            String cartId = req.getRequestURI().substring(7);

            cartsService.addFoodToCart(accessToken, cartId, foodId, count);
            resp.setStatus(204);
            resp.getOutputStream().println();
        } catch (IOException e) {
            resp.setStatus(404);
            resp.getOutputStream().println("Error" + e.getMessage());
        } catch (NoAccessToCartException ex) {
            resp.setStatus(401);
            System.out.println(Cache.getSession(accessToken).getAccessToken() + "////" + accessToken);
            resp.getOutputStream().println(
                    Json.createObjectBuilder()
                            .add("code", "NOT_AUTHORIZED_TO_ACCESS_CART")
                            .add("message", "无权限访问指定的篮子")
                            .build()
                            .toString()
            );
        } catch (CartNotFoundException ex) {
            resp.setStatus(404);
            resp.getOutputStream().println(
                    Json.createObjectBuilder()
                            .add("code", "CART_NOT_FOUND")
                            .add("message", "篮子不存在")
                            .build()
                            .toString()
            );
        } catch (FoodNotFoundException ex) {
            resp.setStatus(404);
            resp.getOutputStream().println(
                    Json.createObjectBuilder()
                            .add("code", "FOOD_NOT_FOUND")
                            .add("message", "食物不存在")
                            .build()
                            .toString()
            );
        } catch (FoodOutOfLimitException ex) {
            resp.setStatus(403);
            resp.getOutputStream().println(
                    Json.createObjectBuilder()
                            .add("code", "FOOD_OUT_OF_LIMIT")
                            .add("message", "篮子中食物数量超过了三个")
                            .build()
                            .toString()
            );
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();
        if (method.toUpperCase().equals("PATCH")) {
            this.doPatch(req, resp);
        } else {
            String protocol = req.getProtocol();
            if (protocol.endsWith("1.1")) {
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Not supported.");
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Not supported.");
            }
        }
    }
}
