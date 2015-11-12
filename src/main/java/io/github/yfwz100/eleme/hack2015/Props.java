package io.github.yfwz100.eleme.hack2015;

/**
 * Get Environment values
 * @author Eric
 */
public class Props {
    public static String APP_HOST = System.getenv("APP_HOST");
    public static String APP_PORT = System.getenv("APP_PORT");

    public static String DB_HOST = System.getenv("DB_HOST");
    public static String DB_PORT = System.getenv("DB_PORT");
    public static String DB_NAME = System.getenv("DB_NAME");
    public static String DB_USER = System.getenv("DB_USER");
    public static String DB_PASS = System.getenv("DB_PASS");

    public static String REDIS_HOST = System.getenv("REDIS_HOST");
    public static String REDIS_PORT = System.getenv("REDIS_PORT");
}
