package Database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionTest {
    @Test
    @DisplayName("Database connection should be successful")
    void testGetConnection() {
        // 1. Singleton instance
        DatabaseManager dbManager = DatabaseManager.getInstance();
        assertNotNull(dbManager, "DatabaseManager's instance should not be null");

        try (Connection connection = dbManager.getConnection()) {
            assertNotNull(connection, "Connection object returned null!");

            assertFalse(connection.isClosed(), "Connection should be open but it seems closed!");

            System.out.println("Database connection test is successful!");
        } catch (SQLException e) {
            fail("Error when connecting database: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Singleton pattern should be implemented")
    void testSingleton() {
        DatabaseManager instance1 = DatabaseManager.getInstance();
        DatabaseManager instance2 = DatabaseManager.getInstance();

        assertSame(instance1, instance2, "Two instances should be same for DatabaseManager object!");
    }
}