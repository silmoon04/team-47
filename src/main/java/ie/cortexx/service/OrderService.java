package ie.cortexx.service;

import ie.cortexx.dao.MerchantDetailsDAO;
import ie.cortexx.dao.OnlineOrderDAO;
import ie.cortexx.dao.OrderDAO;
import ie.cortexx.dao.ProductDAO;
import ie.cortexx.dao.StockDAO;
import ie.cortexx.exception.AuthenticationRequiredException;
import ie.cortexx.exception.ServiceUnavailableException;
import ie.cortexx.model.OnlineOrder;
import ie.cortexx.model.Order;
import ie.cortexx.model.OrderConfirmation;
import ie.cortexx.model.OrderItem;
import ie.cortexx.model.MerchantDetails;
import ie.cortexx.model.Product;
import ie.cortexx.model.StockItem;
import ie.cortexx.util.ProductIdNormalizer;
import ie.cortexx.util.SessionManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// places orders through Team A, tracks delivery status
public class OrderService {
    public enum RemoteSource {
        LIVE_SA("LIVE SA"),
        LOCAL_CACHE("LOCAL CACHE"),
        NONE("SA ISSUE");

        private final String label;

        RemoteSource(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public enum RemoteIssue {
        NONE,
        AUTH_FAILED,
        UNREACHABLE,
        NOT_CONFIGURED
    }

    public record RemoteView<T>(List<T> rows, RemoteSource source, RemoteIssue issue, String message) {
        public RemoteView {
            rows = rows == null ? List.of() : List.copyOf(rows);
            message = message == null ? "" : message;
        }

        public boolean isLive() {
            return source == RemoteSource.LIVE_SA;
        }
    }

    @FunctionalInterface
    private interface RemoteRows<T> {
        List<T> load() throws SQLException;
    }

    private final OrderDAO orderDAO;
    private final StockDAO stockDAO;
    private final OnlineOrderDAO onlineOrderDAO;
    private final ProductDAO productDAO;
    private final MerchantDetailsDAO merchantDetailsDAO;
    private final SAProxyService saProxyService;

    public OrderService() {
        this(new OrderDAO(), new StockDAO(), new OnlineOrderDAO(), new ProductDAO(), new MerchantDetailsDAO(), new SAProxyService());
    }

    public OrderService(OrderDAO orderDAO, StockDAO stockDAO, OnlineOrderDAO onlineOrderDAO) {
        this(orderDAO, stockDAO, onlineOrderDAO, new ProductDAO(), new MerchantDetailsDAO(), new SAProxyService());
    }

    public OrderService(OrderDAO orderDAO, StockDAO stockDAO, OnlineOrderDAO onlineOrderDAO,
                        ProductDAO productDAO, MerchantDetailsDAO merchantDetailsDAO, SAProxyService saProxyService) {
        this.orderDAO = orderDAO;
        this.stockDAO = stockDAO;
        this.onlineOrderDAO = onlineOrderDAO;
        this.productDAO = productDAO;
        this.merchantDetailsDAO = merchantDetailsDAO;
        this.saProxyService = saProxyService;
    }

    public List<StockItem> getCatalogue() throws SQLException {
        return stockDAO.findAll();
    }

    public List<Order> findOrders() throws SQLException {
        return orderDAO.findAllWithItems();
    }

    public Order placeOrder(int productId, int quantity) throws SQLException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        StockItem item = stockDAO.findByProductId(productId);
        if (item == null) {
            throw new IllegalArgumentException("Unknown product id: " + productId);
        }

        int orderedBy = SessionManager.getInstance().getCurrentUser() != null
            ? SessionManager.getInstance().getCurrentUser().getUserId()
            : 1;
        BigDecimal unitPrice = item.getCostPrice();

        Order order = new Order(unitPrice.multiply(BigDecimal.valueOf(quantity)), orderedBy);
        order.getItems().add(new OrderItem(productId, quantity, unitPrice));
        orderDAO.save(order);
        return order;
    }

    public List<OnlineOrder> findOnlineOrders() throws SQLException {
        return onlineOrderDAO.findAll();
    }

    public List<Product> getSaCatalogue() throws SQLException {
        authenticateSa();
        return saProxyService.getCatalogue();
    }

    public RemoteView<Product> loadSaCatalogueView() throws SQLException {
        return loadRemoteView(this::getSaCatalogue, productDAO::findAll,
            "Showing live SA catalogue.", "Showing local cached catalogue");
    }

    public void syncCatalogue() throws SQLException {
        authenticateSa();
        for (Product remote : saProxyService.getCatalogue()) {
            upsertRemoteProduct(remote);
        }
    }

    public OrderConfirmation placeSaOrder(String saProductId, int quantity) throws SQLException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        authenticateSa();
        Product product = ensureLocalProduct(saProductId);

        OrderItem item = new OrderItem(product.getProductId(), quantity, product.getCostPrice());
        OrderConfirmation confirmation = saProxyService.placeOrder(resolveMerchantId(), List.of(item));

        Order localOrder = new Order(product.getCostPrice().multiply(BigDecimal.valueOf(quantity)), currentUserId());
        localOrder.setSaOrderId(confirmation.getSaOrderId());
        localOrder.getItems().add(item);
        try {
            orderDAO.save(localOrder);
        } catch (SQLException error) {
            throw new SQLException("SA order already accepted as " + confirmation.getSaOrderId() + " but local mirror save failed: " + error.getMessage(), error);
        }
        return confirmation;
    }

    public List<Order> findSaOrders() throws SQLException {
        authenticateSa();
        return saProxyService.getOrderHistory(LocalDate.now().minusYears(1), LocalDate.now().plusDays(1));
    }

    public RemoteView<Order> loadSaOrdersView() throws SQLException {
        return loadRemoteView(this::findSaOrders, this::findOrders,
            "Showing live SA order history.", "Showing local order history only");
    }

    private void authenticateSa() throws SQLException {
        MerchantDetails merchantDetails = merchantDetailsDAO.get();
        if (merchantDetails == null) {
            throw new IllegalStateException("Merchant details not configured");
        }
        if (!saProxyService.authenticateMerchant(merchantDetails.getSaUsername(), merchantDetails.getSaPassword())) {
            throw new AuthenticationRequiredException("SA authentication failed for configured merchant");
        }
    }

    private Product ensureLocalProduct(String saProductId) throws SQLException {
        for (Product remote : saProxyService.getCatalogue()) {
            if (!ProductIdNormalizer.matches(saProductId, remote.getSaProductId())) {
                continue;
            }
            return upsertRemoteProduct(remote);
        }

        throw new IllegalArgumentException("Unknown SA product id: " + saProductId);
    }

    private String resolveMerchantId() throws SQLException {
        var merchantDetails = merchantDetailsDAO.get();
        return merchantDetails != null ? merchantDetails.getSaMerchantId() : null;
    }

    private int currentUserId() {
        return SessionManager.getInstance().getCurrentUser() != null
            ? SessionManager.getInstance().getCurrentUser().getUserId()
            : 1;
    }

    private Product copyRemoteProduct(Product remote) {
        Product copy = new Product(remote.getSaProductId(), remote.getName(), remote.getCostPrice(), BigDecimal.ZERO);
        applyRemoteProduct(copy, remote);
        return copy;
    }

    private Product upsertRemoteProduct(Product remote) throws SQLException {
        Product local = productDAO.findBySaProductId(remote.getSaProductId());
        if (local == null) {
            Product copy = copyRemoteProduct(remote);
            productDAO.save(copy);
            return copy;
        }

        applyRemoteProduct(local, remote);
        productDAO.update(local);
        return local;
    }

    private void applyRemoteProduct(Product target, Product remote) {
        target.setSaProductId(remote.getSaProductId());
        target.setName(remote.getName());
        target.setDescription(remote.getDescription());
        target.setPackageType(remote.getPackageType());
        target.setUnitType(remote.getUnitType());
        target.setUnitsPerPack(remote.getUnitsPerPack());
        target.setCostPrice(remote.getCostPrice());
        target.setCategory(remote.getCategory());
        target.setActive(remote.isActive());
        target.setVatRate(BigDecimal.ZERO);
    }

    private <T> RemoteView<T> loadRemoteView(RemoteRows<T> liveRows, RemoteRows<T> fallbackRows,
                                             String liveMessage, String fallbackMessage) throws SQLException {
        try {
            return new RemoteView<>(liveRows.load(), RemoteSource.LIVE_SA, RemoteIssue.NONE, liveMessage);
        } catch (AuthenticationRequiredException error) {
            return fallbackRemoteView(fallbackRows, RemoteIssue.AUTH_FAILED, fallbackMessage, error.getMessage());
        } catch (ServiceUnavailableException error) {
            return fallbackRemoteView(fallbackRows, RemoteIssue.UNREACHABLE, fallbackMessage, error.getMessage());
        } catch (IllegalStateException error) {
            return new RemoteView<>(List.of(), RemoteSource.NONE, RemoteIssue.NOT_CONFIGURED, error.getMessage());
        }
    }

    private <T> RemoteView<T> fallbackRemoteView(RemoteRows<T> fallbackRows, RemoteIssue issue,
                                                 String fallbackMessage, String errorMessage) throws SQLException {
        List<T> rows = fallbackRows.load();
        if (rows.isEmpty()) {
            return new RemoteView<>(List.of(), RemoteSource.NONE, issue, errorMessage);
        }
        return new RemoteView<>(rows, RemoteSource.LOCAL_CACHE, issue, fallbackMessage + ". " + errorMessage);
    }
}