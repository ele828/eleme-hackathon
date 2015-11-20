package io.github.yfwz100.eleme.hack2015.services;

import io.github.yfwz100.eleme.hack2015.database.DatabasePool;
import io.github.yfwz100.eleme.hack2015.database.Cache;
import io.github.yfwz100.eleme.hack2015.exceptions.CartNotFoundException;
import io.github.yfwz100.eleme.hack2015.exceptions.FoodOutOfStockException;
import io.github.yfwz100.eleme.hack2015.exceptions.NoAccessToCartException;
import io.github.yfwz100.eleme.hack2015.exceptions.OrderOutOfLimitException;
import io.github.yfwz100.eleme.hack2015.models.AuthorizedUser;
import io.github.yfwz100.eleme.hack2015.models.Cart;
import io.github.yfwz100.eleme.hack2015.models.Food;
import io.github.yfwz100.eleme.hack2015.models.Order;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * The services for orders.
 *
 * @author yfwz100
 */
public class OrdersService {

    public Order generateOrder(String accessToken, String cartId)
            throws CartNotFoundException, NoAccessToCartException, SQLException, FoodOutOfStockException, OrderOutOfLimitException {
        Cart cart = Cache.getCart(cartId);

        if (cart == null)
            throw new CartNotFoundException();

        if (!cart.getUser().canMakeOrder())
            throw new OrderOutOfLimitException();

        if ( !cart.getUser().getAccessToken().equals(accessToken) )
            throw new NoAccessToCartException();

        Order order = null;

        try (Connection conn = DatabasePool.getConnection()) {
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()) {
                // TODO: To checkValidation stock numbers and count down current numbers. By Sql
                for (Food food : cart.getFoods()) {
                    stmt.addBatch(String.format("update food set stock=stock-%d where stock-%d>=0 and id=%d", food.getCount(), food.getCount(), food.getId()));
                }

                int[] retVal = stmt.executeBatch();
                for (int ret : retVal) {
                    if (ret <= 0)
                        throw new FoodOutOfStockException();
                }
                conn.commit();

                order = new Order(cart.getUser(), cart.getFoods());
                Cache.addOrder(order);
                cart.getUser().setOrder(order);
            } catch (Exception e) {
                conn.rollback();
                throw new FoodOutOfStockException();
            }
        }

        return order;
    }

    public Order getOrder(String accessToken) {
        AuthorizedUser authorizedUser = Cache.getUser(accessToken);
        if (authorizedUser == null)
            return null;
        return authorizedUser.getOrder();
    }

}
