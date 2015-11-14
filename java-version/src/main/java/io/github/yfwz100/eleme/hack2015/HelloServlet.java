package io.github.yfwz100.eleme.hack2015;

import javax.json.Json;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.Data;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The greeting servlet.
 *
 * @author yfwz100
 */
public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("utf-8");

        DatabasePool pool = DatabasePool.getInstance();
        Connection conn = null;
        try {
            conn = pool.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from user;");
            String test = "";
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String pass = rs.getString("password");
                resp.getOutputStream().println(
                        id + "  " + name + "   " + pass
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {}
    }

}
