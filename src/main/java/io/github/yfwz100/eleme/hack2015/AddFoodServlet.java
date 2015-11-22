package io.github.yfwz100.eleme.hack2015;

import io.github.yfwz100.eleme.hack2015.services.CartsService;
import io.github.yfwz100.eleme.hack2015.services.ContextService;
import io.github.yfwz100.eleme.hack2015.services.exceptions.CartNotFoundException;
import io.github.yfwz100.eleme.hack2015.services.exceptions.FoodNotFoundException;
import io.github.yfwz100.eleme.hack2015.services.exceptions.FoodOutOfLimitException;
import io.github.yfwz100.eleme.hack2015.services.exceptions.NoAccessToCartException;

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

    private static final CartsService cartsService = ContextService.getCartsService();

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String accessToken = req.getAttribute(AccessTokenFilter.ACCESS_TOKEN).toString();

        try (JsonReader reader = Json.createReader(req.getInputStream())) {
            JsonObject info = reader.readObject();
            int foodId = info.getInt("food_id");
            int count = info.getInt("count");
            String cartId = req.getRequestURI().substring(7);

            cartsService.addFoodToCart(accessToken, cartId, foodId, count);
            resp.setStatus(204);
            resp.getOutputStream().println();
        } catch (IOException e) {
            resp.setStatus(404);
            resp.getOutputStream().println("Error" + e.getMessage());
        } catch (NoAccessToCartException ex) {
            resp.setStatus(401);
            resp.getOutputStream().println("{\"code\":\"NOT_AUTHORIZED_TO_ACCESS_CART\",\"message\":\"无权限访问指定的篮子\"}");
        } catch (CartNotFoundException ex) {
            resp.setStatus(404);
            resp.getOutputStream().println("{\"code\":\"CART_NOT_FOUND\",\"message\":\"篮子不存在\"}");
        } catch (FoodNotFoundException ex) {
            resp.setStatus(404);
            resp.getOutputStream().println("{\"code\":\"FOOD_NOT_FOUND\",\"message\":\"食物不存在\"}");
        } catch (FoodOutOfLimitException ex) {
            resp.setStatus(403);
            resp.getOutputStream().println("{\"code\":\"FOOD_OUT_OF_LIMIT\",\"message\":\"篮子中食物数量超过了三个\"}");
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
