package ie.cortexx.dao;

import ie.cortexx.model.Template;
import java.sql.*;

// handles SQL for `templates` table
// only a few rows (FIRST, SECOND), looked up by type not id
// settings panel uses findByType to load, update to save changes
public class TemplateDAO {

    // TODO: SELECT * FROM templates WHERE template_type = ?
    public Template findByType(String type) throws SQLException {
        return null;
    }

    // TODO: UPDATE templates SET content = ?, updated_by = ? WHERE template_id = ?
    public void update(Template template) throws SQLException {
    }
}
