package io.github.yfwz100.eleme.hack2015;

import io.github.yfwz100.eleme.hack2015.exceptions.UserNotFoundException;
import io.github.yfwz100.eleme.hack2015.models.AuthorizedUser;
import io.github.yfwz100.eleme.hack2015.services.AccessTokenService;

import javax.json.Json;
import javax.json.JsonException;
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

    private AccessTokenService accessTokenService = new AccessTokenService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("utf-8");

        try (JsonReader reader = Json.createReader(req.getInputStream())) {
            JsonObject info = reader.readObject();

            String username = info.getString("username");
            String password = info.getString("password");

            AuthorizedUser authorizedUser = accessTokenService.checkUserPassword(username, password);

            resp.getOutputStream().println(
                    Json.createObjectBuilder()
                            .add("user_id", authorizedUser.getUser().getId())
                            .add("username", authorizedUser.getUser().getName())
                            .add("access_token", authorizedUser.getAccessToken())
                            .build()
                            .toString()
            );

        } catch (UserNotFoundException e) {
            resp.setStatus(403);
            resp.getOutputStream().println(
                    Json.createObjectBuilder()
                            .add("code", "USER_AUTH_FAIL")
                            .add("message", "用户名或密码错误")
                            .build()
                            .toString()
            );
        } catch (JsonException e) {
            resp.setStatus(400);
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
        this.doPost(req, resp);
    }
}
