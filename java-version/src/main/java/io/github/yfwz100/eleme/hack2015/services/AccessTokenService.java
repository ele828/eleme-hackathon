package io.github.yfwz100.eleme.hack2015.services;

import io.github.yfwz100.eleme.hack2015.DatabasePool;
import io.github.yfwz100.eleme.hack2015.Storage;
import io.github.yfwz100.eleme.hack2015.models.User;

import java.sql.*;
import java.util.UUID;

/**
 * The mock of access token service.
 *
 * @author yfwz100
 */
public class AccessTokenService {

    public User checkUserPassword(String username, String password) {
        DatabasePool pool = DatabasePool.getInstance();
        Connection conn = null;
        try {
            conn = pool.getConnection();
            String sql = "select * from user where name=? and password=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                String accessToken = generateAccessToken();
                User user = new User(rs.getInt("id"), rs.getString("name"), rs.getString("password"), accessToken);
                Storage.addUser(accessToken, user);
                return user;
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public String generateAccessToken() {
        return UUID.randomUUID().toString();
    }

    public User checkAccessToken(String accessToken) {
        return Storage.getUser(accessToken);
    }
}