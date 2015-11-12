package io.github.yfwz100.eleme.hack2015;

import io.github.yfwz100.eleme.hack2015.services.AccessTokenService;
import io.github.yfwz100.eleme.hack2015.services.mock.AccessTokenServiceMock;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The servlet used to perform login authentication.
 *
 * @author yfwz100
 */
public class LoginServlet extends HttpServlet {

    // FIXME: 15/11/12 Implements the access token service.
    private AccessTokenService accessTokenService = new AccessTokenServiceMock();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (JsonReader reader = Json.createReader(req.getInputStream())) {
            JsonObject info = reader.readObject();

            String username = info.getString("username");
            String password = info.getString("password");

            if (accessTokenService.checkUserPassword(username, password)) {
                resp.getOutputStream().println(
                        Json.createObjectBuilder()
                                .add("user_id", 1)
                                .add("username", username)
                                .add("access_token", accessTokenService.generateAccessToken(username, password))
                                .build()
                                .toString()
                );
            } else {
                resp.setStatus(403);
                resp.getOutputStream().println(
                        Json.createObjectBuilder()
                                .add("code", "USER_AUTH_FAIL")
                                .add("message", "用户名或密码错误")
                                .build()
                                .toString()
                );
            }
        } catch (Exception e) {
            resp.setStatus(404);
            resp.getOutputStream().println("Error 404 NotFound");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }
}
