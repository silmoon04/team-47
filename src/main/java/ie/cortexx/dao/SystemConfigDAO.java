package ie.cortexx.dao;

import ie.cortexx.util.DBConnection;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class SystemConfigDAO {

    public Map<String, String> findAll() throws SQLException {
        String sql = "SELECT config_key, config_value FROM system_config ORDER BY config_key";
        Map<String, String> config = new LinkedHashMap<>();

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql);
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                config.put(rs.getString("config_key"), rs.getString("config_value"));
            }
        }

        return config;
    }

    public String findValue(String key) throws SQLException {
        String sql = "SELECT config_value FROM system_config WHERE config_key = ?";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public void saveValue(String key, String value) throws SQLException {
        String sql = "INSERT INTO system_config (config_key, config_value) VALUES (?, ?) "
            + "ON DUPLICATE KEY UPDATE config_value = VALUES(config_value)";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        }
    }
}