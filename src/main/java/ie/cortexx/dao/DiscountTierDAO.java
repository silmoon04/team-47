package ie.cortexx.dao;

import ie.cortexx.model.DiscountTier;
import ie.cortexx.util.DBConnection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DiscountTierDAO {

    public List<DiscountTier> findByCustomer(int customerId) throws SQLException {
        String sql = "SELECT * FROM discount_tiers WHERE customer_id = ? ORDER BY min_monthly_spend";
        List<DiscountTier> tiers = new ArrayList<>();

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    DiscountTier tier = new DiscountTier();
                    tier.setTierId(rs.getInt("tier_id"));
                    tier.setCustomerId(rs.getInt("customer_id"));
                    tier.setTierName(rs.getString("tier_name"));
                    tier.setMinMonthlySpend(rs.getBigDecimal("min_monthly_spend"));
                    tier.setDiscountRate(rs.getBigDecimal("discount_rate"));
                    tiers.add(tier);
                }
            }
        }

        return tiers;
    }
}