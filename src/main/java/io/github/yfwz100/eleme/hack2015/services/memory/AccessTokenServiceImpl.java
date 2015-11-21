package io.github.yfwz100.eleme.hack2015.services.memory;

import io.github.yfwz100.eleme.hack2015.database.Cache;
import io.github.yfwz100.eleme.hack2015.database.DatabasePool;
import io.github.yfwz100.eleme.hack2015.services.exceptions.UserNotFoundException;
import io.github.yfwz100.eleme.hack2015.models.Session;
import io.github.yfwz100.eleme.hack2015.models.User;
import io.github.yfwz100.eleme.hack2015.services.AccessTokenService;

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
public class AccessTokenServiceImpl implements AccessTokenService {

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

    @Override
    public Session checkUserPassword(String username, String password) throws UserNotFoundException {
        User user = userMap.get(username);
        if (user != null && user.getPass().equals(password)) {
            String accessToken = generateAccessToken();
            Session session = new Session(user, accessToken);
            Cache.addSession(accessToken, session);
            return session;
        } else {
            throw new UserNotFoundException();
        }
    }

    @Override
    public String generateAccessToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Session checkAccessToken(String accessToken) {
        return Cache.getSession(accessToken);
    }

    private static final AccessTokenService accessTokenService = new AccessTokenServiceImpl();

    private AccessTokenServiceImpl() {
    }

    public static AccessTokenService getInstance() {
        return accessTokenService;
    }
}