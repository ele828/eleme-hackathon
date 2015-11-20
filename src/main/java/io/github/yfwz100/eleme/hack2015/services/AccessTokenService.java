package io.github.yfwz100.eleme.hack2015.services;

import io.github.yfwz100.eleme.hack2015.database.Cache;
import io.github.yfwz100.eleme.hack2015.database.DatabasePool;
import io.github.yfwz100.eleme.hack2015.exceptions.UserNotFoundException;
import io.github.yfwz100.eleme.hack2015.models.AuthorizedUser;
import io.github.yfwz100.eleme.hack2015.models.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
        try (
                Connection conn = DatabasePool.getConnection();
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

    public AuthorizedUser checkUserPassword(String username, String password) throws UserNotFoundException {
        User user = userMap.get(username);
        if (user != null && user.getPass().equals(password)) {
            String accessToken = generateAccessToken();
            AuthorizedUser authorizedUser = new AuthorizedUser(user, accessToken);
            Cache.addUser(accessToken, authorizedUser);
            return authorizedUser;
        } else {
            throw new UserNotFoundException();
        }
    }

    public String generateAccessToken() {
        return UUID.randomUUID().toString();
    }

    public AuthorizedUser checkAccessToken(String accessToken) {
        return Cache.getUser(accessToken);
    }
}