package database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    
    private static HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://zephyr.proxy.rlwy.net:53829/railway?useSSL=false&allowPublicKeyRetrieval=true");
            config.setUsername("root");
            config.setPassword("McosHeFoqsnZmjTqLnvpDEKJPajzGsnY");
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setMaximumPoolSize(10); 
            config.setMinimumIdle(2);      
            config.setIdleTimeout(30000);  
            config.setConnectionTimeout(10000); 

            dataSource = new HikariDataSource(config);
            System.out.println("HikariCP Connection Pool Initialized Successfully!");
        } catch (Exception e) {
            System.err.println("Failed to initialize database connection pool."); e.printStackTrace();
        }
    }

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException { return dataSource.getConnection(); }

    public static void closeConnection() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close(); System.out.println("Database pool safely closed.");
        }
    }
}