package io.github.yfwz100.eleme.hack2015;

import io.github.yfwz100.eleme.hack2015.services.OrdersService;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 * The entrance for ele.me hackathon 2015.
 *
 * @author yfwz100
 */
public class AppServer {

    public static void main(String... args) throws Exception {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8080);
        server.setConnectors(new Connector[]{connector});
        ServletContextHandler context = new ServletContextHandler();

        context.setContextPath("/");

        context.addServlet(HelloServlet.class, "/hello");
        context.addServlet(LoginServlet.class, "/login");

//        context.addFilter(AccessTokenFilter.class, "/foods", EnumSet.of(DispatcherType.REQUEST));
        context.addServlet(FoodsServlet.class, "/foods");

        context.addFilter(AccessTokenFilter.class, "/carts", EnumSet.of(DispatcherType.REQUEST));
        context.addServlet(CartsServlet.class, "/carts");

        context.addFilter(AccessTokenFilter.class, "/carts/*", EnumSet.of(DispatcherType.REQUEST));
        context.addServlet(NewFoodServlet.class, "/carts/*");

        context.addFilter(AccessTokenFilter.class, "/orders*", EnumSet.of(DispatcherType.REQUEST));
        context.addServlet(OrderServlet.class, "/orders");

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[]{context, new DefaultHandler()});
        server.setHandler(handlers);
        server.start();
        server.join();
    }
}
