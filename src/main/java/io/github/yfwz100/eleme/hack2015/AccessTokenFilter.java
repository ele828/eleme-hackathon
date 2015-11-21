package io.github.yfwz100.eleme.hack2015;

import io.github.yfwz100.eleme.hack2015.services.AccessTokenService;
import io.github.yfwz100.eleme.hack2015.services.memory.AccessTokenServiceImpl;

import javax.json.Json;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Check if the access token is valid.
 *
 * @author yfwz100
 */
public class AccessTokenFilter implements Filter {

    public static final String ACCESS_TOKEN = "actk";
    private AccessTokenService accessTokenService = AccessTokenServiceImpl.getInstance();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = ((HttpServletRequest) request);
        HttpServletResponse resp = ((HttpServletResponse) response);
        resp.setCharacterEncoding("utf-8");

        String accessToken = request.getParameter("access_token");
        if (accessToken == null || accessToken.isEmpty()) {
            accessToken = req.getHeader("Access-Token");
        }
        req.setAttribute(ACCESS_TOKEN, accessToken);

        if (accessToken != null && accessTokenService.checkAccessToken(accessToken) != null) {
            chain.doFilter(request, response);
        } else {
            resp.setStatus(401);
            resp.getOutputStream().println(
                    Json.createObjectBuilder()
                            .add("code", "INVALID_ACCESS_TOKEN")
                            .add("message", "无效的令牌")
                            .build()
                            .toString()
            );
        }
    }

    @Override
    public void destroy() {

    }
}
