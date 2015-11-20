package io.github.yfwz100.eleme.hack2015.util;

/**
 * Get Environment values
 * @author Eric
 */
public class Props {
    public static String APP_HOST = System.getenv("APP_HOST");
    public static int APP_PORT = Integer.parseInt(System.getenv("APP_PORT"));

    public static String DB_HOST = System.getenv("DB_HOST");
    public static int DB_PORT = Integer.parseInt(System.getenv("DB_PORT"));
    public static String DB_NAME = System.getenv("DB_NAME");
    public static String DB_USER = System.getenv("DB_USER");
    public static String DB_PASS = System.getenv("DB_PASS");

    public static String REDIS_HOST = System.getenv("REDIS_HOST");
    public static int REDIS_PORT = Integer.parseInt(System.getenv("REDIS_PORT"));
}
