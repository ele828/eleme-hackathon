package io.github.yfwz100.eleme.hack2015.services;

import io.github.yfwz100.eleme.hack2015.DatabasePool;
import io.github.yfwz100.eleme.hack2015.Storage;
import io.github.yfwz100.eleme.hack2015.exceptions.CartNotFoundException;
import io.github.yfwz100.eleme.hack2015.exceptions.FoodOutOfStockException;
import io.github.yfwz100.eleme.hack2015.exceptions.NoAccessToCartException;
import io.github.yfwz100.eleme.hack2015.exceptions.OrderOutOfLimitException;
import io.github.yfwz100.eleme.hack2015.models.Cart;
import io.github.yfwz100.eleme.hack2015.models.Food;
import io.github.yfwz100.eleme.hack2015.models.Order;
import io.github.yfwz100.eleme.hack2015.models.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * The services for orders.
 *
 * @author yfwz100
 */
public class OrdersService {

    public Order generateOrder(String accessToken, String cartId)
            throws CartNotFoundException, NoAccessToCartException, SQLException, FoodOutOfStockException, OrderOutOfLimitException {

        Cart cart = Storage.getCart(cartId);
        if (!cart.getUser().canMakeOrder())
            throw new OrderOutOfLimitException();

        if (cart == null)
            throw new CartNotFoundException();
        if ( !cart.getUser().getAccessToken().equals(accessToken) )
            throw new NoAccessToCartException();

        Connection conn = DatabasePool.getInstance().getConnection();
        Statement stmt = conn.createStatement();
        conn.setAutoCommit(false);

        Order order = null;

        try {
            // TODO: To checkValidation stock numbers and count down current numbers. By Sql
            for (Food food : cart.getFoods()) {
                stmt.addBatch("update food set stock=stock-"+food.getCount()+" where stock-"+food.getCount()+">=0 and id="+ food.getId() +";");
            }

            int[] retVal = stmt.executeBatch();
            for (int ret : retVal) {
                if (ret <= 0)
                    throw new FoodOutOfStockException();
            }
            conn.commit();

            order = new Order(cart.getUser(), cart.getFoods());
            Storage.addOrder(order);
            cart.getUser().setOrder(order);
        } catch (Exception e) {
            // Roll back
            conn.rollback();
            throw new FoodOutOfStockException();

        } finally {
            stmt.close();
            conn.close();
        }
        return order;
    }

    public Map<String, Order> gerOrders() {
        return Storage.getOrders();
    }

    public Order getOrder(String accessToken) {
        User user = Storage.getUser(accessToken);
        if (user == null)
            return null;
        return user.getOrder();
    }

}
