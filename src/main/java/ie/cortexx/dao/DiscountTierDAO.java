package ie.cortexx.dao;

import ie.cortexx.model.DiscountTier;
import ie.cortexx.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
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

    public void save(DiscountTier tier) throws SQLException {
        String sql = "INSERT INTO discount_tiers (customer_id, tier_name, min_monthly_spend, discount_rate) "
            + "VALUES (?, ?, ?, ?)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindTier(ps, tier);
            ps.executeUpdate();

            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    tier.setTierId(rs.getInt(1));
                }
            }
        }
    }

    public void update(DiscountTier tier) throws SQLException {
        String sql = "UPDATE discount_tiers SET customer_id = ?, tier_name = ?, min_monthly_spend = ?, discount_rate = ? "
            + "WHERE tier_id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bindTier(ps, tier);
            ps.setInt(5, tier.getTierId());
            ps.executeUpdate();
        }
    }

    public void delete(int tierId) throws SQLException {
        String sql = "DELETE FROM discount_tiers WHERE tier_id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, tierId);
            ps.executeUpdate();
        }
    }

    public DiscountTier findById(int tierId) throws SQLException {
        String sql = "SELECT * FROM discount_tiers WHERE tier_id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, tierId);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                DiscountTier tier = new DiscountTier();
                tier.setTierId(rs.getInt("tier_id"));
                tier.setCustomerId(rs.getInt("customer_id"));
                tier.setTierName(rs.getString("tier_name"));
                tier.setMinMonthlySpend(rs.getBigDecimal("min_monthly_spend"));
                tier.setDiscountRate(rs.getBigDecimal("discount_rate"));
                return tier;
            }
        }
    }

    private void bindTier(PreparedStatement ps, DiscountTier tier) throws SQLException {
        if (tier.getCustomerId() == null) {
            ps.setNull(1, java.sql.Types.INTEGER);
        } else {
            ps.setInt(1, tier.getCustomerId());
        }
        ps.setString(2, tier.getTierName());
        ps.setBigDecimal(3, tier.getMinMonthlySpend());
        ps.setBigDecimal(4, tier.getDiscountRate());
    }

    public void deleteByCustomer(int customerId) throws SQLException {
        String sql = "DELETE FROM discount_tiers WHERE customer_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
        }
    }
}
