package ie.cortexx.service;

import ie.cortexx.dao.ProductDAO;
import ie.cortexx.enums.OrderStatus;
import ie.cortexx.exception.*;
import ie.cortexx.interfaces.I_SAtoCA;
import ie.cortexx.model.Order;
import ie.cortexx.model.OrderConfirmation;
import ie.cortexx.model.OrderItem;
import ie.cortexx.model.Product;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;

public class SAProxyService implements I_SAtoCA {
    private final ProductDAO productDAO;
    private final String saUrl;
    private final String dbUser;
    private final String dbPassword;

    private Integer authenticatedMerchantUserId;
    private String authenticatedUsername;
    private String authenticatedAccountNo;

    public SAProxyService() {
        this(new ProductDAO(), loadSaUrl(), loadProperty("db.user"), loadProperty("db.password"));
    }

    SAProxyService(ProductDAO productDAO, String saUrl, String dbUser, String dbPassword) {
        this.productDAO = productDAO;
        this.saUrl = saUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    @Override
    public boolean authenticateMerchant(String username, String password)
            throws IllegalArgumentException {
        if (isBlank(username) || isBlank(password)) {
            throw new IllegalArgumentException("username and password are required");
        }

        String sql = "SELECT UserID, Username, AccountNo FROM Users WHERE Username = ? AND Password = ? AND Role = 'Merchant'";

        try (var c = openSaConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, password);

            try (var rs = ps.executeQuery()) {
                if (!rs.next()) {
                    clearAuth();
                    return false;
                }

                authenticatedMerchantUserId = rs.getInt("UserID");
                authenticatedUsername = rs.getString("Username");
                authenticatedAccountNo = rs.getString("AccountNo");
                return true;
            }
        } catch (SQLException error) {
            throw new ServiceUnavailableException("Unable to reach SA database: " + error.getMessage());
        }
    }

    @Override
    public List<Product> getCatalogue()
            throws AuthenticationRequiredException, ServiceUnavailableException {
        requireAuthenticated();
        String sql = "SELECT ProductID, Name, Description, Category, PackageType, Unit, UnitsInPack, PackageCost, Availability, StockLimit, IsActive "
            + "FROM Catalogue WHERE IsActive = TRUE ORDER BY ProductID";
        List<Product> products = new ArrayList<>();

        try (var c = openSaConnection();
             var ps = c.prepareStatement(sql);
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                products.add(mapCatalogueProduct(rs));
            }
            return products;
        } catch (SQLException error) {
            throw new ServiceUnavailableException("Unable to read SA catalogue: " + error.getMessage());
        }
    }

    @Override
    public OrderConfirmation placeOrder(String merchantID, List<OrderItem> items)
            throws IllegalArgumentException, InsufficientStockException, AuthenticationRequiredException {
        requireAuthenticated();
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items are required");
        }
        if (!merchantMatches(merchantID)) {
            throw new IllegalArgumentException("merchant id does not match authenticated merchant");
        }

        List<ResolvedItem> resolvedItems = resolveItems(items);
        String orderId;

        try (var c = openSaConnection()) {
            boolean previousAutoCommit = c.getAutoCommit();
            c.setAutoCommit(false);

            try {
                for (ResolvedItem item : resolvedItems) {
                    int available = getAvailableStock(c, item.saProductId());
                    if (available < item.quantity()) {
                        throw new InsufficientStockException("SA stock too low for " + item.saProductId());
                    }
                }

                orderId = nextOrderId(c);
                BigDecimal total = resolvedItems.stream()
                    .map(item -> item.unitCost().multiply(BigDecimal.valueOf(item.quantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                try (var ps = c.prepareStatement(
                    "INSERT INTO Orders (OrderID, MerchantID, TotalAmount, OrderStatus, EstimatedDelivery, DispatchDetails) VALUES (?, ?, ?, ?, ?, ?)")) {
                    ps.setString(1, orderId);
                    ps.setInt(2, authenticatedMerchantUserId);
                    ps.setBigDecimal(3, total);
                    ps.setString(4, "ACCEPTED");
                    ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now().plusDays(2)));
                    ps.setString(6, null);
                    ps.executeUpdate();
                }

                try (var ps = c.prepareStatement("INSERT INTO OrderItems (OrderID, ProductID, Quantity, UnitCost) VALUES (?, ?, ?, ?)")) {
                    for (ResolvedItem item : resolvedItems) {
                        ps.setString(1, orderId);
                        ps.setString(2, item.saProductId());
                        ps.setInt(3, item.quantity());
                        ps.setBigDecimal(4, item.unitCost());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                try (var ps = c.prepareStatement("UPDATE Catalogue SET Availability = Availability - ? WHERE ProductID = ?")) {
                    for (ResolvedItem item : resolvedItems) {
                        ps.setInt(1, item.quantity());
                        ps.setString(2, item.saProductId());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                c.commit();
            } catch (SQLException | InsufficientStockException error) {
                c.rollback();
                if (error instanceof InsufficientStockException stockError) {
                    throw stockError;
                }
                throw new ServiceUnavailableException("Unable to place SA order: " + error.getMessage());
            } finally {
                c.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException error) {
            throw new ServiceUnavailableException("Unable to place SA order: " + error.getMessage());
        }

        return new OrderConfirmation(orderId, "ACCEPTED", "Order accepted by SA");
    }

    @Override
    public OrderStatus getOrderStatus(String orderId)
            throws IllegalArgumentException, OrderNotFoundException {
        requireAuthenticated();
        if (isBlank(orderId)) {
            throw new IllegalArgumentException("order id is required");
        }

        String sql = "SELECT OrderStatus FROM Orders WHERE OrderID = ? AND MerchantID = ?";
        try (var c = openSaConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setString(1, orderId.trim());
            ps.setInt(2, authenticatedMerchantUserId);

            try (var rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new OrderNotFoundException("Unknown SA order id: " + orderId);
                }
                return mapOrderStatus(rs.getString(1));
            }
        } catch (SQLException error) {
            throw new ServiceUnavailableException("Unable to load SA order status: " + error.getMessage());
        }
    }

    @Override
    public BigDecimal getOutstandingBalance()
            throws AuthenticationRequiredException, ServiceUnavailableException {
        requireAuthenticated();
        String sql = "SELECT OutstandingBalance FROM Users WHERE UserID = ?";

        try (var c = openSaConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setInt(1, authenticatedMerchantUserId);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new AuthenticationRequiredException("Authenticated merchant not found in SA database");
                }
                return rs.getBigDecimal(1);
            }
        } catch (SQLException error) {
            throw new ServiceUnavailableException("Unable to load SA outstanding balance: " + error.getMessage());
        }
    }

    @Override
    public List<Order> getOrderHistory(LocalDate fromDate, LocalDate toDate)
            throws IllegalArgumentException, AuthenticationRequiredException, ServiceUnavailableException {
        requireAuthenticated();
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("fromDate and toDate are required");
        }
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }

        String sql = "SELECT OrderID, MerchantID, OrderDate, TotalAmount, OrderStatus, EstimatedDelivery "
            + "FROM Orders WHERE MerchantID = ? AND OrderDate >= ? AND OrderDate < ? ORDER BY OrderDate DESC";
        List<Order> orders = new ArrayList<>();

        try (var c = openSaConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setInt(1, authenticatedMerchantUserId);
            ps.setTimestamp(2, Timestamp.valueOf(fromDate.atStartOfDay()));
            ps.setTimestamp(3, Timestamp.valueOf(toDate.plusDays(1).atStartOfDay()));

            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setSaOrderId(rs.getString("OrderID"));
                    order.setMerchantId(rs.getInt("MerchantID"));
                    order.setOrderStatus(mapOrderStatus(rs.getString("OrderStatus")));
                    order.setTotalAmount(rs.getBigDecimal("TotalAmount"));
                    Timestamp orderDate = rs.getTimestamp("OrderDate");
                    if (orderDate != null) {
                        order.setOrderedAt(orderDate.toLocalDateTime());
                    }
                    Timestamp estimatedDelivery = rs.getTimestamp("EstimatedDelivery");
                    if (estimatedDelivery != null && order.getOrderStatus() == OrderStatus.DELIVERED) {
                        order.setDeliveredAt(estimatedDelivery.toLocalDateTime());
                    }
                    order.setOrderedBy(authenticatedMerchantUserId);
                    order.setItems(loadOrderItems(c, order.getSaOrderId()));
                    orders.add(order);
                }
            }

            return orders;
        } catch (SQLException error) {
            throw new ServiceUnavailableException("Unable to load SA order history: " + error.getMessage());
        }
    }

    protected Connection openSaConnection() throws SQLException {
        return DriverManager.getConnection(saUrl, dbUser, dbPassword);
    }

    private static String loadSaUrl() {
        Properties props = loadProperties();
        String explicit = props.getProperty("db.sa.url");
        if (!isBlank(explicit)) {
            return explicit.trim();
        }

        String caUrl = props.getProperty("db.url");
        int slash = caUrl.lastIndexOf('/');
        int query = caUrl.indexOf('?', slash);
        if (query >= 0) {
            return caUrl.substring(0, slash + 1) + "ipos_sa_db" + caUrl.substring(query);
        }
        return caUrl.substring(0, slash + 1) + "ipos_sa_db";
    }

    private static String loadProperty(String key) {
        String value = loadProperties().getProperty(key);
        if (value == null) {
            throw new IllegalStateException("Missing property: " + key);
        }
        return value;
    }

    private static Properties loadProperties() {
        try (InputStream in = SAProxyService.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("db.properties not found in classpath");
            }
            Properties props = new Properties();
            props.load(in);
            return props;
        } catch (IOException error) {
            throw new IllegalStateException("Unable to load db.properties", error);
        }
    }

    private Product mapCatalogueProduct(java.sql.ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setSaProductId(rs.getString("ProductID"));
        product.setName(rs.getString("Name"));
        product.setDescription(rs.getString("Description"));
        product.setCategory(rs.getString("Category"));
        product.setPackageType(rs.getString("PackageType"));
        product.setUnitType(rs.getString("Unit"));
        product.setUnitsPerPack(rs.getInt("UnitsInPack"));
        product.setCostPrice(rs.getBigDecimal("PackageCost"));
        product.setAvailability(rs.getInt("Availability"));
        product.setStockLimit(rs.getInt("StockLimit"));
        product.setActive(rs.getBoolean("IsActive"));
        product.setMarkupRate(BigDecimal.ZERO);
        product.setVatRate(BigDecimal.ZERO);
        product.setLastSynced(LocalDateTime.now());

        try {
            Product localMirror = productDAO.findBySaProductId(product.getSaProductId());
            if (localMirror != null) {
                product.setProductId(localMirror.getProductId());
                product.setMarkupRate(localMirror.getMarkupRate() != null ? localMirror.getMarkupRate() : BigDecimal.ZERO);
                product.setVatRate(localMirror.getVatRate() != null ? localMirror.getVatRate() : BigDecimal.ZERO);
            }
        } catch (SQLException ignored) {
        }

        return product;
    }

    private List<OrderItem> loadOrderItems(Connection c, String orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT ProductID, Quantity, UnitCost FROM OrderItems WHERE OrderID = ?";

        try (var ps = c.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    String saProductId = rs.getString("ProductID");
                    Product localProduct = null;
                    try {
                        localProduct = productDAO.findBySaProductId(saProductId);
                    } catch (SQLException ignored) {
                    }
                    item.setProductId(localProduct != null ? localProduct.getProductId() : 0);
                    item.setQuantity(rs.getInt("Quantity"));
                    item.setUnitPrice(rs.getBigDecimal("UnitCost"));
                    items.add(item);
                }
            }
        }

        return items;
    }

    private int getAvailableStock(Connection c, String saProductId) throws SQLException {
        try (var ps = c.prepareStatement("SELECT Availability FROM Catalogue WHERE ProductID = ? FOR UPDATE")) {
            ps.setString(1, saProductId);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private String nextOrderId(Connection c) throws SQLException {
        int year = LocalDate.now().getYear();
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(OrderID, '-', -1) AS UNSIGNED)), 0) FROM Orders WHERE YEAR(OrderDate) = ?";
        try (var ps = c.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (var rs = ps.executeQuery()) {
                rs.next();
                int next = rs.getInt(1) + 1;
                return String.format("ORD-%d-%05d", year, next);
            }
        }
    }

    private List<ResolvedItem> resolveItems(List<OrderItem> items) {
        List<ResolvedItem> resolved = new ArrayList<>();
        for (OrderItem item : items) {
            try {
                Product product = productDAO.findById(item.getProductId());
                if (product == null || isBlank(product.getSaProductId())) {
                    throw new IllegalArgumentException("No SA product mapping for local product " + item.getProductId());
                }
                BigDecimal unitCost = item.getUnitPrice() != null ? item.getUnitPrice() : product.getCostPrice();
                resolved.add(new ResolvedItem(product.getSaProductId(), item.getQuantity(), unitCost));
            } catch (SQLException error) {
                throw new ServiceUnavailableException("Unable to resolve local product mapping: " + error.getMessage());
            }
        }
        return resolved;
    }

    private OrderStatus mapOrderStatus(String status) {
        return switch (status) {
            case "ACCEPTED" -> OrderStatus.ACCEPTED;
            case "PROCESSING" -> OrderStatus.PROCESSING;
            case "READY_TO_DISPATCH" -> OrderStatus.PACKED;
            case "DISPATCHED" -> OrderStatus.DISPATCHED;
            case "DELIVERED" -> OrderStatus.DELIVERED;
            case "CANCELLED" -> OrderStatus.CANCELLED;
            default -> OrderStatus.PROCESSING;
        };
    }

    private boolean merchantMatches(String merchantID) {
        if (isBlank(merchantID)) {
            return true;
        }

        String normalized = normalizeMerchantId(merchantID);
        return normalized.equals(normalizeMerchantId(authenticatedAccountNo))
            || normalized.equals(normalizeMerchantId(authenticatedUsername))
            || normalized.equals(normalizeMerchantId(String.valueOf(authenticatedMerchantUserId)));
    }

    private String normalizeMerchantId(String value) {
        return value == null ? "" : value.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    private void requireAuthenticated() {
        if (authenticatedMerchantUserId == null) {
            throw new AuthenticationRequiredException("Authenticate with SA before calling this method");
        }
    }

    private void clearAuth() {
        authenticatedMerchantUserId = null;
        authenticatedUsername = null;
        authenticatedAccountNo = null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record ResolvedItem(String saProductId, int quantity, BigDecimal unitCost) {
    }
}
