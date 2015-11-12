package io.github.yfwz100.eleme.hack2015;

import io.github.yfwz100.eleme.hack2015.models.User;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Eric on 15/11/12.
 */
public class OrderServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String accessToken = req.getParameter("access_token");

        try (JsonReader reader = Json.createReader(req.getInputStream())) {
            JsonObject info = reader.readObject();
            String cartId = info.getString("cart_id");





        } catch (Exception e) {
            resp.setStatus(404);
            resp.getOutputStream().println("Error" + e.getMessage());
        }

    }
}
