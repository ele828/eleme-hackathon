package io.github.yfwz100.eleme.hack2015;

import io.github.yfwz100.eleme.hack2015.services.AccessTokenService;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Check if the access token is valid.
 *
 * @author yfwz100
 */
public class AccessTokenFilter implements Filter {

    // FIXME: 15/11/12 Implement the access token service.
    private AccessTokenService accessTokenService = new AccessTokenService();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String accessToken = request.getParameter("access_token");
//        if(accessToken.equals(""))
//            accessToken = request.getHeader("Access-Token");

        if (accessTokenService.checkAccessToken(accessToken) != null) {
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).sendError(403);
        }
    }

    @Override
    public void destroy() {

    }
}
