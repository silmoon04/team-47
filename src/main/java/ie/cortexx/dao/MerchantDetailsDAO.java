package ie.cortexx.dao;

import ie.cortexx.model.MerchantDetails;
import ie.cortexx.util.DBConnection;
import java.sql.*;

// handles SQL for `merchant_details` (singleton row)
public class MerchantDetailsDAO {

    public MerchantDetails get() throws SQLException {
        String sql = "SELECT * FROM merchant_details WHERE merchant_id = 1";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql);
             var rs = ps.executeQuery()) {
            if (!rs.next()) {
                return null;
            }
            return mapMerchantDetails(rs);
        }
    }

    public void update(MerchantDetails md) throws SQLException {
        String sql = "UPDATE merchant_details SET business_name = ?, address = ?, phone = ?, email = ?, "
            + "sa_merchant_id = ?, sa_username = ?, sa_password = ? WHERE merchant_id = ?";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            // keep this tiny, settings only edits one row
            ps.setString(1, md.getBusinessName());
            ps.setString(2, md.getAddress());
            ps.setString(3, md.getPhone());
            ps.setString(4, md.getEmail());
            ps.setString(5, md.getSaMerchantId());
            ps.setString(6, md.getSaUsername());
            ps.setString(7, md.getSaPassword());
            ps.setInt(8, md.getMerchantId());
            ps.executeUpdate();
        }
    }

    private MerchantDetails mapMerchantDetails(ResultSet rs) throws SQLException {
        MerchantDetails details = new MerchantDetails();
        details.setMerchantId(rs.getInt("merchant_id"));
        details.setBusinessName(rs.getString("business_name"));
        details.setAddress(rs.getString("address"));
        details.setPhone(rs.getString("phone"));
        details.setEmail(rs.getString("email"));
        details.setSaMerchantId(rs.getString("sa_merchant_id"));
        details.setSaUsername(rs.getString("sa_username"));
        details.setSaPassword(rs.getString("sa_password"));
        return details;
    }
}
