package io.github.yfwz100.eleme.hack2015.database;

import io.github.yfwz100.eleme.hack2015.util.Props;
import org.apache.commons.dbcp.BasicDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database connection pool
 * @author Eric
 */
public class DatabasePool {
    private static BasicDataSource ds = null;

    static {
        String url = String.format("jdbc:mysql://%s:%s/%s", Props.DB_HOST, Props.DB_PORT, Props.DB_NAME);
        ds = new BasicDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUrl(url);
        ds.setUsername(Props.DB_USER);
        ds.setPassword(Props.DB_PASS);
        ds.setMaxWait(5000);
        ds.setInitialSize(50);
        ds.setMaxActive(100);
        ds.setMinIdle(25);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void close() throws SQLException {
        ds.close();
    }

}
