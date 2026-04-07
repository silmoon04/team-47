package ie.cortexx.dao;

import ie.cortexx.model.Template;
import ie.cortexx.util.DBConnection;
import java.sql.*;

// handles SQL for `templates` table
// only a few rows (FIRST, SECOND), looked up by type not id
// settings panel uses findByType to load, update to save changes
public class TemplateDAO {

    // SELECT * FROM templates WHERE template_type = ?
    public Template findByType(String type) throws SQLException {
        String sql = "SELECT * FROM templates WHERE template_type = ?";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setString(1, type);

            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapTemplate(rs);
            }
        }
    }
    private Template mapTemplate(ResultSet rs) throws SQLException {
        Template t = new Template();
        t.setTemplateId(rs.getInt("template_id"));
        t.setTemplateType(rs.getString("template_type"));
        t.setContent(rs.getString("content"));

        int updatedBy = rs.getInt("updated_by");
        if (rs.wasNull()) {
            t.setUpdatedBy(null);
        } else {
            t.setUpdatedBy(updatedBy);
        }

        return t;
    }

    // UPDATE templates SET content = ?, updated_by = ? WHERE template_id = ?
    public void update(Template template) throws SQLException {
        String sql = "UPDATE templates SET content = ?, updated_by = ? WHERE template_id = ?";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setString(1, template.getContent());

            if (template.getUpdatedBy() != null) {
                ps.setInt(2, template.getUpdatedBy());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            ps.setInt(3, template.getTemplateId());
            ps.executeUpdate();
        }
    }
}
