package ie.cortexx.service;

import ie.cortexx.dao.MerchantDetailsDAO;
import ie.cortexx.dao.OnlineOrderDAO;
import ie.cortexx.dao.OrderDAO;
import ie.cortexx.dao.ProductDAO;
import ie.cortexx.dao.StockDAO;
import ie.cortexx.model.OnlineOrder;
import ie.cortexx.model.Order;
import ie.cortexx.model.OrderConfirmation;
import ie.cortexx.model.OrderItem;
import ie.cortexx.model.Product;
import ie.cortexx.model.StockItem;
import ie.cortexx.util.SessionManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// places orders through Team A, tracks delivery status
public class OrderService {
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
        List<Order> orders = new ArrayList<>();
        for (Order order : orderDAO.findAll()) {
            orders.add(orderDAO.findById(order.getOrderId()));
        }
        return orders;
    }

    public Order placeOrder(int productId, int quantity) throws SQLException {
        StockItem item = stockDAO.findByProductId(productId);
        if (item == null) {
            return null;
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
        try {
            authenticateSa();
            return saProxyService.getCatalogue();
        } catch (RuntimeException error) {
            return productDAO.findAll();
        }
    }

    public void syncCatalogue() throws SQLException {
        authenticateSa();
        for (Product remote : saProxyService.getCatalogue()) {
            Product local = productDAO.findBySaProductId(remote.getSaProductId());
            if (local == null) {
                Product copy = new Product(remote.getSaProductId(), remote.getName(), remote.getCostPrice(), BigDecimal.ZERO);
                copy.setDescription(remote.getDescription());
                copy.setPackageType(remote.getPackageType());
                copy.setUnitType(remote.getUnitType());
                copy.setUnitsPerPack(remote.getUnitsPerPack());
                copy.setCategory(remote.getCategory());
                copy.setVatRate(BigDecimal.ZERO);
                copy.setActive(remote.isActive());
                productDAO.save(copy);
            } else {
                local.setName(remote.getName());
                local.setDescription(remote.getDescription());
                local.setPackageType(remote.getPackageType());
                local.setUnitType(remote.getUnitType());
                local.setUnitsPerPack(remote.getUnitsPerPack());
                local.setCostPrice(remote.getCostPrice());
                local.setCategory(remote.getCategory());
                local.setActive(remote.isActive());
                productDAO.update(local);
            }
        }
    }

    public OrderConfirmation placeSaOrder(String saProductId, int quantity) throws SQLException {
        Product product = ensureLocalProduct(saProductId);
        authenticateSa();

        OrderItem item = new OrderItem(product.getProductId(), quantity, product.getCostPrice());
        OrderConfirmation confirmation = saProxyService.placeOrder(resolveMerchantId(), List.of(item));

        Order localOrder = new Order(product.getCostPrice().multiply(BigDecimal.valueOf(quantity)), currentUserId());
        localOrder.setSaOrderId(confirmation.getSaOrderId());
        localOrder.getItems().add(item);
        orderDAO.save(localOrder);
        return confirmation;
    }

    public List<Order> findSaOrders() throws SQLException {
        try {
            authenticateSa();
            return saProxyService.getOrderHistory(LocalDate.now().minusYears(1), LocalDate.now().plusDays(1));
        } catch (RuntimeException error) {
            return findOrders();
        }
    }

    private void authenticateSa() throws SQLException {
        var merchantDetails = merchantDetailsDAO.get();
        if (merchantDetails == null) {
            throw new IllegalStateException("Merchant details not configured");
        }
        if (!saProxyService.authenticateMerchant(merchantDetails.getSaUsername(), merchantDetails.getSaPassword())) {
            throw new IllegalStateException("SA authentication failed for configured merchant");
        }
    }

    private Product ensureLocalProduct(String saProductId) throws SQLException {
        Product product = productDAO.findBySaProductId(saProductId);
        if (product != null) {
            return product;
        }

        for (Product remote : getSaCatalogue()) {
            if (!saProductId.equals(remote.getSaProductId())) {
                continue;
            }
            Product copy = new Product(remote.getSaProductId(), remote.getName(), remote.getCostPrice(), BigDecimal.ZERO);
            copy.setDescription(remote.getDescription());
            copy.setPackageType(remote.getPackageType());
            copy.setUnitType(remote.getUnitType());
            copy.setUnitsPerPack(remote.getUnitsPerPack());
            copy.setCategory(remote.getCategory());
            copy.setVatRate(BigDecimal.ZERO);
            copy.setActive(remote.isActive());
            productDAO.save(copy);
            return copy;
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
}