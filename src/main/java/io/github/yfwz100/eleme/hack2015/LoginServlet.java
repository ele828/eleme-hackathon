package io.github.yfwz100.eleme.hack2015;

import io.github.yfwz100.eleme.hack2015.models.Session;
import io.github.yfwz100.eleme.hack2015.services.AccessTokenService;
import io.github.yfwz100.eleme.hack2015.services.ContextService;
import io.github.yfwz100.eleme.hack2015.services.exceptions.UserNotFoundException;

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

    private static final String USER_NOT_FOUND_JSON = Json.createObjectBuilder()
            .add("code", "USER_AUTH_FAIL")
            .add("message", "用户名或密码错误")
            .build()
            .toString();
    private static final String MALFORMED_JSON = Json.createObjectBuilder()
            .add("code", "MALFORMED_JSON")
            .add("message", "格式错误")
            .build()
            .toString();

    private final AccessTokenService accessTokenService = ContextService.getAccessTokenService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("utf-8");

        try (JsonReader reader = Json.createReader(req.getInputStream())) {
            JsonObject info = reader.readObject();

            String username = info.getString("username");
            String password = info.getString("password");

            Session session = accessTokenService.checkUserPassword(username, password);

            resp.getOutputStream().println(
                    Json.createObjectBuilder()
                            .add("user_id", session.getUser().getId())
                            .add("username", session.getUser().getName())
                            .add("access_token", session.getAccessToken())
                            .build()
                            .toString()
            );

        } catch (UserNotFoundException e) {
            resp.setStatus(403);
            resp.getOutputStream().println(USER_NOT_FOUND_JSON);
        } catch (JsonException e) {
            resp.setStatus(400);
            resp.getOutputStream().println(MALFORMED_JSON);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }
}
