package io.github.yfwz100.eleme.hack2015;

import io.github.yfwz100.eleme.hack2015.models.User;
import io.github.yfwz100.eleme.hack2015.services.AccessTokenService;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
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
    private AccessTokenService accessTokenService = new AccessTokenService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("utf-8");

        System.out.println(">>>>>>>>>>>>>>>>>>>" + req.getContentLength());
        if( req.getContentLength() == -1 || req.getContentLength() == 0 ) {
            resp.setStatus(400);
            resp.getOutputStream().println(
                    Json.createObjectBuilder()
                            .add("code", "EMPTY_REQUEST")
                            .add("message", "请求体为空")
                            .build()
                            .toString()
            );
            return;
        }

        try (JsonReader reader = Json.createReader(req.getInputStream())) {
            JsonObject info = reader.readObject();

            String username = info.getString("username");
            String password = info.getString("password");

            User user = null;
            if ((user = accessTokenService.checkUserPassword(username, password)) != null) {
                resp.getOutputStream().println(
                        Json.createObjectBuilder()
                                .add("user_id", user.getId())
                                .add("username", user.getName())
                                .add("access_token", user.getAccessToken())
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
