package ie.cortexx.service;

import ie.cortexx.dao.MerchantDetailsDAO;
import ie.cortexx.dao.OnlineOrderDAO;
import ie.cortexx.dao.OrderDAO;
import ie.cortexx.dao.ProductDAO;
import ie.cortexx.dao.StockDAO;
import ie.cortexx.enums.UserRole;
import ie.cortexx.exception.ServiceUnavailableException;
import ie.cortexx.model.MerchantDetails;
import ie.cortexx.model.OnlineOrder;
import ie.cortexx.model.Order;
import ie.cortexx.model.OrderConfirmation;
import ie.cortexx.model.OrderItem;
import ie.cortexx.model.Product;
import ie.cortexx.model.StockItem;
import ie.cortexx.model.User;
import ie.cortexx.util.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {
    private record Harness(
        OrderService service,
        OrderDAO orderDAO,
        StockDAO stockDAO,
        OnlineOrderDAO onlineOrderDAO,
        ProductDAO productDAO,
        MerchantDetailsDAO merchantDetailsDAO,
        SAProxyService saProxyService
    ) {}

    @AfterEach
    void clearSession() {
        SessionManager.getInstance().logout();
    }

    @Test
    void place_order_uses_session_user_and_stock_cost() throws Exception {
        Harness harness = harness();

        StockItem stockItem = new StockItem();
        stockItem.setProductId(4);
        stockItem.setCostPrice(new BigDecimal("2.50"));
        when(harness.stockDAO().findByProductId(4)).thenReturn(stockItem);

        SessionManager.getInstance().login(user(7));

        Order order = harness.service().placeOrder(4, 3);

        assertNotNull(order);
        assertEquals(7, order.getOrderedBy());
        assertEquals(new BigDecimal("7.50"), order.getTotalAmount());
        verify(harness.orderDAO()).save(any(Order.class));
    }

    @Test
    void find_orders_loads_full_orders() throws Exception {
        Harness harness = harness();

        Order shallow = new Order();
        shallow.setOrderId(5);
        Order full = new Order();
        full.setOrderId(5);
        full.setOrderedAt(LocalDateTime.now());
        when(harness.orderDAO().findAll()).thenReturn(List.of(shallow));
        when(harness.orderDAO().findById(5)).thenReturn(full);

        assertEquals(1, harness.service().findOrders().size());
        verify(harness.orderDAO()).findById(5);
    }

    @Test
    void find_online_orders_delegates() throws Exception {
        Harness harness = harness();
        when(harness.onlineOrderDAO().findAll()).thenReturn(List.of(new OnlineOrder(), new OnlineOrder()));

        assertEquals(2, harness.service().findOnlineOrders().size());
    }

    @Test
    void place_order_rejects_non_positive_quantity() {
        Harness harness = harness();

        assertThrows(IllegalArgumentException.class, () -> harness.service().placeOrder(4, 0));
    }

    @Test
    void load_sa_catalogue_view_marks_local_cache_when_auth_fails() throws Exception {
        Harness harness = harness();

        when(harness.merchantDetailsDAO().get()).thenReturn(merchant("ACC-0001", "admin", "bad-pass"));
        when(harness.saProxyService().authenticateMerchant("admin", "bad-pass")).thenReturn(false);
        when(harness.productDAO().findAll()).thenReturn(List.of(product("T-0100")));

        OrderService.RemoteView<Product> view = harness.service().loadSaCatalogueView();

        assertEquals(OrderService.RemoteSource.LOCAL_CACHE, view.source());
        assertEquals(OrderService.RemoteIssue.AUTH_FAILED, view.issue());
        assertEquals(1, view.rows().size());
    }

    @Test
    void load_sa_orders_view_marks_local_cache_when_sa_is_unreachable() throws Exception {
        Harness harness = harness();

        Order shallow = new Order();
        shallow.setOrderId(5);
        Order full = new Order();
        full.setOrderId(5);

        stubAuthenticatedMerchant(harness);
        when(harness.saProxyService().getOrderHistory(any(), any())).thenThrow(new ServiceUnavailableException("sa down"));
        when(harness.orderDAO().findAll()).thenReturn(List.of(shallow));
        when(harness.orderDAO().findById(5)).thenReturn(full);

        OrderService.RemoteView<Order> view = harness.service().loadSaOrdersView();

        assertEquals(OrderService.RemoteSource.LOCAL_CACHE, view.source());
        assertEquals(OrderService.RemoteIssue.UNREACHABLE, view.issue());
        assertEquals(1, view.rows().size());
    }

    @Test
    void place_sa_order_uses_live_sa_cost_when_local_mirror_is_stale() throws Exception {
        Harness harness = harness();

        Product local = product("T-0100");
        local.setProductId(4);
        local.setCostPrice(new BigDecimal("1.00"));
        Product remote = product("T-0100");
        remote.setCostPrice(new BigDecimal("5.00"));

        stubAuthenticatedMerchant(harness);
        when(harness.saProxyService().getCatalogue()).thenReturn(List.of(remote));
        when(harness.productDAO().findBySaProductId("T-0100")).thenReturn(local);
        when(harness.saProxyService().placeOrder(eq("ACC-0001"), argThat(items -> items.size() == 1 && matchUnitPrice(items.get(0), "5.00"))))
            .thenReturn(new OrderConfirmation("ORD-2026-00001", "ACCEPTED", "ok"));

        OrderConfirmation confirmation = harness.service().placeSaOrder("T 0100", 2);

        assertEquals("ORD-2026-00001", confirmation.getSaOrderId());
        assertEquals(0, new BigDecimal("5.00").compareTo(local.getCostPrice()));
        verify(harness.productDAO()).update(local);
    }

    @Test
    void place_sa_order_surfaces_confirmed_remote_id_when_local_save_fails() throws Exception {
        Harness harness = harness();

        Product remote = product("T-0100");
        remote.setProductId(4);
        remote.setCostPrice(new BigDecimal("5.00"));

        stubAuthenticatedMerchant(harness);
        when(harness.saProxyService().getCatalogue()).thenReturn(List.of(remote));
        when(harness.productDAO().findBySaProductId("T-0100")).thenReturn(remote);
        when(harness.saProxyService().placeOrder(any(), any())).thenReturn(new OrderConfirmation("ORD-2026-00002", "ACCEPTED", "ok"));
        doThrow(new SQLException("local write failed")).when(harness.orderDAO()).save(any(Order.class));

        SQLException error = assertThrows(SQLException.class, () -> harness.service().placeSaOrder("T-0100", 1));

        assertTrue(error.getMessage().contains("ORD-2026-00002"));
    }

    private Harness harness() {
        OrderDAO orderDAO = mock(OrderDAO.class);
        StockDAO stockDAO = mock(StockDAO.class);
        OnlineOrderDAO onlineOrderDAO = mock(OnlineOrderDAO.class);
        ProductDAO productDAO = mock(ProductDAO.class);
        MerchantDetailsDAO merchantDetailsDAO = mock(MerchantDetailsDAO.class);
        SAProxyService saProxyService = mock(SAProxyService.class);
        OrderService service = new OrderService(orderDAO, stockDAO, onlineOrderDAO, productDAO, merchantDetailsDAO, saProxyService);
        return new Harness(service, orderDAO, stockDAO, onlineOrderDAO, productDAO, merchantDetailsDAO, saProxyService);
    }

    private boolean matchUnitPrice(OrderItem item, String amount) {
        return item != null && item.getUnitPrice() != null && item.getUnitPrice().compareTo(new BigDecimal(amount)) == 0;
    }

    private void stubAuthenticatedMerchant(Harness harness) throws Exception {
        when(harness.merchantDetailsDAO().get()).thenReturn(merchant("ACC-0001", "admin", "admin123"));
        when(harness.saProxyService().authenticateMerchant("admin", "admin123")).thenReturn(true);
    }

    private MerchantDetails merchant(String merchantId, String username, String password) {
        MerchantDetails details = new MerchantDetails();
        details.setMerchantId(1);
        details.setSaMerchantId(merchantId);
        details.setSaUsername(username);
        details.setSaPassword(password);
        return details;
    }

    private Product product(String saProductId) {
        Product product = new Product();
        product.setSaProductId(saProductId);
        product.setName("Demo");
        product.setCostPrice(new BigDecimal("1.00"));
        product.setPackageType("Box");
        product.setUnitType("Pack");
        product.setUnitsPerPack(1);
        product.setCategory("Test");
        product.setActive(true);
        return product;
    }

    private User user(int userId) {
        User user = new User("demo", "demo", "Demo User", UserRole.MANAGER);
        user.setUserId(userId);
        return user;
    }
}