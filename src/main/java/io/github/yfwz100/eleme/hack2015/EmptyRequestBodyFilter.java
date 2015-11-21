package io.github.yfwz100.eleme.hack2015;

import javax.json.Json;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The empty request filter.
 *
 * @author eric
 */
public class EmptyRequestBodyFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request.getInputStream().available() > 0) {
            chain.doFilter(request, response);
        } else {
            HttpServletResponse resp = ((HttpServletResponse) response);
            resp.setStatus(400);
            resp.setCharacterEncoding("utf-8");
            resp.getOutputStream().println(
                    Json.createObjectBuilder()
                            .add("code", "EMPTY_REQUEST")
                            .add("message", "请求体为空")
                            .build()
                            .toString()
            );
        }
    }

    @Override
    public void destroy() {
    }
}
