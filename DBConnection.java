
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralized MySQL connection settings.
 * Environment variables override defaults.
 */
public class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/gamepulse";

    private static String setting(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    public static Connection getConnection() throws SQLException {
        String url = setting("GAMEPULSE_DB_URL", DEFAULT_URL);
        String user = setting("GAMEPULSE_DB_USER", "root");
        String password = setting("GAMEPULSE_DB_PASSWORD", "");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException(
                "MySQL Connector/J is missing. Keep the JAR in the lib folder and on the classpath.",
                ex
            );
        }

        return DriverManager.getConnection(url, user, password);
    }
}
