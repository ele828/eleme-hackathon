package io.github.yfwz100.eleme.hack2015;

import io.github.yfwz100.eleme.hack2015.exceptions.CartNotFoundException;
import io.github.yfwz100.eleme.hack2015.exceptions.FoodNotFoundException;
import io.github.yfwz100.eleme.hack2015.exceptions.FoodOutOfLimitException;
import io.github.yfwz100.eleme.hack2015.exceptions.NoAccessToCartException;
import io.github.yfwz100.eleme.hack2015.services.CartsService;
import io.github.yfwz100.eleme.hack2015.services.FoodsService;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * Created by Eric on 15/11/12.
 */
public class NewFoodServlet extends HttpServlet{

    CartsService cartsService = new CartsService();

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("utf-8");

        String accessToken = Utils.checkValidation(req, resp);
        if( accessToken.equals("") )
            return;

        try (JsonReader reader = Json.createReader(req.getInputStream())) {
            JsonObject info = reader.readObject();
            int foodId = info.getInt("food_id");
            int count = info.getInt("count");
            String cartId = req.getRequestURI().replace("/carts/", "");

            cartsService.addFoodToCart(accessToken, cartId, foodId, count);
            resp.setStatus(204);
        } catch (IOException e) {
            resp.setStatus(404);
            resp.getOutputStream().println("Error" + e.getMessage());
        } catch (NoAccessToCartException ex) {
            resp.setStatus(401);
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
        resp.getOutputStream().println(

        );
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();
        long errMsg;
        if(method.equals("PATCH")) {
            errMsg = this.getLastModified(req);
            if(errMsg == -1L) {
                this.doPatch(req, resp);
            } else {
                long ifModifiedSince = req.getDateHeader("If-Modified-Since");
                if(ifModifiedSince < errMsg) {
                    this.doPatch(req, resp);
                } else {
                    resp.setStatus(304);
                }
            }
        } else if(method.equals("GET")) {
            errMsg = this.getLastModified(req);
            if(errMsg == -1L) {
                super.doGet(req, resp);
            } else {
                long ifModifiedSince = req.getDateHeader("If-Modified-Since");
                if(ifModifiedSince < errMsg) {
                    super.doGet(req, resp);
                } else {
                    resp.setStatus(304);
                }
            }
        } else if(method.equals("HEAD")) {
            super.doHead(req, resp);
        } else if(method.equals("POST")) {
            super.doPost(req, resp);
        } else if(method.equals("PUT")) {
            super.doPut(req, resp);
        } else if(method.equals("DELETE")) {
            super.doDelete(req, resp);
        } else if(method.equals("OPTIONS")) {
            super.doOptions(req, resp);
        } else if(method.equals("TRACE")) {
            super.doTrace(req, resp);
        }
    }
}
