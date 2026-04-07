package ie.cortexx.dao;

import ie.cortexx.model.Customer;
import ie.cortexx.model.MerchantDetails;
import ie.cortexx.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// handles SQL for `merchant_details` (singleton row)
public class MerchantDetailsDAO {

    public MerchantDetails get() throws SQLException {
        String sql = "SELECT * FROM merchant WHERE merchant_id = ?";

        try (var d = DBConnection.getConnection();
            var ps = d.prepareStatement(sql)){

            try (var rs = ps.executeQuery()){
                if (!rs.next()) return null;
                return mapMerchant(rs);
            }
        }
    }

    public void update(MerchantDetails md) throws SQLException {
        String sql = "UPDATE merchant_details SET merchant_id = ?, business_name = ?, " +
            "address = ?, phone = ?, email = ?, sa_merchant_id = ?, sa_username = ?, " +
            "sa_password = ? WHERE merchant_id = ?";

        try (var d = DBConnection.getConnection();

            var ps = d.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, md.getMerchantId());
            ps.setString(2, md.getBusinessName());
            ps.setString(3, md.getAddress());
            ps.setString(4, md.getPhone());
            ps.setString(5, md.getEmail());
            ps.setString(6, md.getSaMerchantId());
            ps.setString(7, md.getSaUsername());
            ps.setString(8, md.getSaPassword());

            ps.executeUpdate();
        }
    }

    private Customer mapMerchant(ResultSet rs) throws SQLException {
        return MerchantDetails.MfromRS(rs);
    }
}
