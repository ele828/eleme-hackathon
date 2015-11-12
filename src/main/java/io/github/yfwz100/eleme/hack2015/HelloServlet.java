package io.github.yfwz100.eleme.hack2015;

import javax.json.Json;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The greeting servlet.
 *
 * @author yfwz100
 */
public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("utf-8");
        resp.getOutputStream().println(
                Json.createObjectBuilder()
                        .add("cart_id", "你好")
                        .build().toString()
        );
    }

}
