package io.github.yfwz100.eleme.hack2015.services;

import io.github.yfwz100.eleme.hack2015.database.DatabasePool;
import io.github.yfwz100.eleme.hack2015.database.Cache;
import io.github.yfwz100.eleme.hack2015.exceptions.UserNotFoundException;
import io.github.yfwz100.eleme.hack2015.models.User;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The mock of access token service.
 *
 * @author yfwz100
 */
public class AccessTokenService {

    private static Map<String, User> userMap = new HashMap<>();

    static {
        DatabasePool pool = DatabasePool.getInstance();
        try (
                Connection conn = pool.getConnection();
                Statement stat = conn.createStatement();
                ResultSet resultSet = stat.executeQuery("select * from user")
        ) {
            while (resultSet.next()) {
                userMap.put(
                        resultSet.getString("name"),
                        new User(
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getString("password")
                        )
                );
            }
        } catch (SQLException ignored) {
        }
    }

    public User checkUserPassword(String username, String password) throws UserNotFoundException {
        User user = userMap.get(username);
        if (user != null && user.getPass().equals(password)) {
            String accessToken = generateAccessToken();
//            user.setAccessToken(accessToken);
            User user1 =new User(user.getId(), username, password, accessToken);
            Cache.addUser(accessToken, user1);
            return user1;
        } else {
            throw new UserNotFoundException();
        }
    }


    public String generateAccessToken() {
        return UUID.randomUUID().toString();
    }

    public User checkAccessToken(String accessToken) {
        return Cache.getUser(accessToken);
    }
}