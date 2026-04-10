package ie.cortexx.service;

import ie.cortexx.dao.DiscountTierDAO;
import ie.cortexx.dao.SaleDAO;
import ie.cortexx.enums.DiscountType;
import ie.cortexx.model.Customer;
import ie.cortexx.model.DiscountTier;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

// calculates customer discount (fixed rate or flexible tier)
public class DiscountService {
    private final SaleDAO saleDAO;
    private final DiscountTierDAO discountTierDAO;

    public DiscountService() {
        this(new SaleDAO(), new DiscountTierDAO());
    }

    public DiscountService(SaleDAO saleDAO, DiscountTierDAO discountTierDAO) {
        this.saleDAO = saleDAO;
        this.discountTierDAO = discountTierDAO;
    }

    public BigDecimal resolveRate(Customer customer, BigDecimal pendingSubtotal, LocalDate saleDate) throws SQLException {
        if (customer == null || customer.getDiscountType() == null) {
            return BigDecimal.ZERO;
        }

        if (customer.getDiscountType() == DiscountType.FIXED) {
            return safeRate(customer.getFixedDiscountRate());
        }

        List<DiscountTier> tiers = discountTierDAO.findByCustomer(customer.getCustomerId());
        if (tiers.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal monthSpend = currentMonthSpend(customer.getCustomerId(), saleDate).add(safeMoney(pendingSubtotal));
        return tiers.stream()
            .filter(tier -> safeMoney(tier.getMinMonthlySpend()).compareTo(monthSpend) <= 0)
            .max(Comparator.comparing(tier -> safeMoney(tier.getMinMonthlySpend())))
            .map(DiscountTier::getDiscountRate)
            .map(this::safeRate)
            .orElse(BigDecimal.ZERO);
    }

    public BigDecimal resolveDiscount(Customer customer, BigDecimal pendingSubtotal, LocalDate saleDate) throws SQLException {
        return safeMoney(pendingSubtotal)
            .multiply(resolveRate(customer, pendingSubtotal, saleDate))
            .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public BigDecimal currentMonthSpend(int customerId, LocalDate saleDate) throws SQLException {
        YearMonth targetMonth = YearMonth.from(saleDate != null ? saleDate : LocalDate.now());
        return saleDAO.findByCustomer(customerId).stream()
            .filter(sale -> sale.getSaleDate() != null)
            .filter(sale -> YearMonth.from(sale.getSaleDate().toLocalDate()).equals(targetMonth))
            .map(sale -> safeMoney(sale.getSubtotal() != null ? sale.getSubtotal() : sale.getTotalAmount()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal safeRate(BigDecimal rate) {
        return rate != null ? rate : BigDecimal.ZERO;
    }

    private BigDecimal safeMoney(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }
}