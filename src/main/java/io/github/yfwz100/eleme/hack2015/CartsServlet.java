package io.github.yfwz100.eleme.hack2015;

import io.github.yfwz100.eleme.hack2015.models.Cart;
import io.github.yfwz100.eleme.hack2015.models.User;
import io.github.yfwz100.eleme.hack2015.services.CartsService;

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

    // FIXME: 15/11/12 Implement carts service.
    private CartsService cartsService = new CartsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String accessToken = req.getParameter("access_token");

        User user = Storage.getUser(accessToken);

        Cart cart = new Cart(user);
        Storage.addCart(cart);

        resp.getOutputStream().println(
                Json.createObjectBuilder()
                        .add("cart_id", cart.getCartId())
                        .build().toString()
        );
    }

}
