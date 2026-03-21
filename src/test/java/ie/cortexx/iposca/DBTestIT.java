package ie.cortexx.iposca;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DBTestIT {

    @Test
    void testDemoDB() throws Exception {
        Connection conn = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/demo_db", "demo_user", "demo_pass");
        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM demo_table");
        rs.next();
        int rowCount = rs.getInt(1);
        assertEquals(0, rowCount, "DB should be empty after reset");

        conn.close();
    }
}
