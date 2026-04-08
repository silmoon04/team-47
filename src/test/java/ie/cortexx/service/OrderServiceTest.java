package ie.cortexx.service;

import ie.cortexx.dao.OnlineOrderDAO;
import ie.cortexx.dao.OrderDAO;
import ie.cortexx.dao.StockDAO;
import ie.cortexx.enums.UserRole;
import ie.cortexx.model.OnlineOrder;
import ie.cortexx.model.Order;
import ie.cortexx.model.StockItem;
import ie.cortexx.model.User;
import ie.cortexx.util.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @AfterEach
    void clearSession() {
        SessionManager.getInstance().logout();
    }

    @Test
    void place_order_uses_session_user_and_stock_cost() throws Exception {
        OrderDAO orderDAO = mock(OrderDAO.class);
        StockDAO stockDAO = mock(StockDAO.class);
        OnlineOrderDAO onlineOrderDAO = mock(OnlineOrderDAO.class);
        OrderService service = new OrderService(orderDAO, stockDAO, onlineOrderDAO);

        StockItem stockItem = new StockItem();
        stockItem.setProductId(4);
        stockItem.setCostPrice(new BigDecimal("2.50"));
        when(stockDAO.findByProductId(4)).thenReturn(stockItem);

        SessionManager.getInstance().login(user(7));

        Order order = service.placeOrder(4, 3);

        assertNotNull(order);
        assertEquals(7, order.getOrderedBy());
        assertEquals(new BigDecimal("7.50"), order.getTotalAmount());
        verify(orderDAO).save(any(Order.class));
    }

    @Test
    void find_orders_loads_full_orders() throws Exception {
        OrderDAO orderDAO = mock(OrderDAO.class);
        StockDAO stockDAO = mock(StockDAO.class);
        OnlineOrderDAO onlineOrderDAO = mock(OnlineOrderDAO.class);
        OrderService service = new OrderService(orderDAO, stockDAO, onlineOrderDAO);

        Order shallow = new Order();
        shallow.setOrderId(5);
        Order full = new Order();
        full.setOrderId(5);
        full.setOrderedAt(LocalDateTime.now());
        when(orderDAO.findAll()).thenReturn(List.of(shallow));
        when(orderDAO.findById(5)).thenReturn(full);

        assertEquals(1, service.findOrders().size());
        verify(orderDAO).findById(5);
    }

    @Test
    void find_online_orders_delegates() throws Exception {
        OrderDAO orderDAO = mock(OrderDAO.class);
        StockDAO stockDAO = mock(StockDAO.class);
        OnlineOrderDAO onlineOrderDAO = mock(OnlineOrderDAO.class);
        OrderService service = new OrderService(orderDAO, stockDAO, onlineOrderDAO);
        when(onlineOrderDAO.findAll()).thenReturn(List.of(new OnlineOrder(), new OnlineOrder()));

        assertEquals(2, service.findOnlineOrders().size());
    }

    private User user(int userId) {
        User user = new User("demo", "demo", "Demo User", UserRole.MANAGER);
        user.setUserId(userId);
        return user;
    }
}