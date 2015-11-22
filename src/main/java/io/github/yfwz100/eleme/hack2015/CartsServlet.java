package io.github.yfwz100.eleme.hack2015;

import io.github.yfwz100.eleme.hack2015.models.Cart;
import io.github.yfwz100.eleme.hack2015.services.CartsService;
import io.github.yfwz100.eleme.hack2015.services.ContextService;

import javax.json.Json;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The carts servlet.
 *
 * @author yfwz100
 */
public class CartsServlet extends HttpServlet {

    private static final CartsService cartsService = ContextService.getCartsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String accessToken = req.getAttribute(AccessTokenFilter.ACCESS_TOKEN).toString();

        Cart cart = cartsService.createCart(accessToken);

        resp.getOutputStream().println(
                Json.createObjectBuilder()
                        .add("cart_id", cart.getCartId())
                        .build().toString()
        );
    }

}
