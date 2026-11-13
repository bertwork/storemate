package model.repository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Opens connections. Settings come from /resources/db.properties, with Laragon defaults as fallback. */
public class Database {

    private static final String CONFIG_FILE = "/resources/db.properties";

    private static final Properties config = loadConfig();

    private static Properties loadConfig() {
        Properties defaults = new Properties();
        defaults.setProperty("db.url",
                "jdbc:mysql://localhost:3306/db_storemate?useSSL=false&allowPublicKeyRetrieval=true");
        defaults.setProperty("db.user", "root");
        defaults.setProperty("db.password", "");

        Properties props = new Properties(defaults);
        try (InputStream in = Database.class.getResourceAsStream(CONFIG_FILE)) {
            if (in != null) props.load(in);
        } catch (IOException e) {
            System.err.println("Could not read " + CONFIG_FILE + ", using defaults");
        }
        return props;
    }

    /** Caller closes it (use try-with-resources). */
    public static Connection connect() throws SQLException {
        Properties login = new Properties();
        login.setProperty("user", config.getProperty("db.user"));
        login.setProperty("password", config.getProperty("db.password"));
        login.setProperty("connectTimeout", "3000"); // fail after 3 s instead of hanging when MySQL is down
        return DriverManager.getConnection(config.getProperty("db.url"), login);
    }

    /** True if the database answers. Used for the status light in the header. */
    public static boolean isReachable() {
        try (Connection con = connect()) {
            return con.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}