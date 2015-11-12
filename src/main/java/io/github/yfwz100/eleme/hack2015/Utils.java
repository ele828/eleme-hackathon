package io.github.yfwz100.eleme.hack2015;

import io.github.yfwz100.eleme.hack2015.services.AccessTokenService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Created by Eric on 15/11/12.
 */
public class Utils {

    public static boolean checkLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String accessToken = request.getParameter("access_token");
        if(accessToken == null)
            accessToken = request.getHeader("Access-Token");

        System.out.println("Access: " + accessToken);
        AccessTokenService accessTokenService = new AccessTokenService();
        if (accessTokenService.checkAccessToken(accessToken) != null) {
            return true;
        } else {
            response.sendError(403);
            return false;
        }
    }
}
