package io.github.yfwz100.eleme.hack2015;

import io.github.yfwz100.eleme.hack2015.services.AccessTokenService;

import javax.json.Json;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Created by Eric on 15/11/12.
 */
public class Utils {

    public static String checkValidation(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String accessToken = request.getParameter("access_token");
        if(accessToken == null)
            accessToken = request.getHeader("Access-Token");

        System.out.println("Access: " + accessToken);

        AccessTokenService accessTokenService = new AccessTokenService();
        if (accessTokenService.checkAccessToken(accessToken) != null) {
            return accessToken;
        } else {
            response.setCharacterEncoding("utf-8");
            response.setStatus(401);
            response.getOutputStream().println(
                    Json.createObjectBuilder()
                            .add("code", "INVALID_ACCESS_TOKEN")
                            .add("message", "无效的令牌")
                            .build()
                            .toString()
            );
            return "";
        }
    }
}
