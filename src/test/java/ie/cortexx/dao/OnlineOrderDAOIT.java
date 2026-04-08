package ie.cortexx.dao;

import ie.cortexx.model.OnlineOrder;
import ie.cortexx.model.OnlineOrderItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static ie.cortexx.TestDatabaseHelper.*;
import static org.junit.jupiter.api.Assertions.*;

class OnlineOrderDAOIT {

    @Test
    void saveAssignsIdAndRoundTripsTeamCHeader() throws Exception {
        OnlineOrderDAO dao = new OnlineOrderDAO();
        OnlineOrder order = order("PU-IT-" + System.currentTimeMillis(), "CONFIRMED", bd("12.50"), bd("2.50"), List.of(
            item(1, 1, bd("15.00"))
        ));

        dao.save(order);

        try {
            OnlineOrder saved = dao.findAll().stream()
                .filter(x -> x.getOnlineOrderId() == order.getOnlineOrderId())
                .findFirst()
                .orElseThrow();

            assertTrue(order.getOnlineOrderId() > 0);
            assertEquals(order.getMemberId(), saved.getMemberId());
            assertEquals(order.getOrderReference(), saved.getOrderReference());
            assertEquals(order.getTotalPrice(), saved.getTotalPrice());
            assertEquals(order.getDiscountApplied(), saved.getDiscountApplied());
            assertEquals(order.getPaymentMethod(), saved.getPaymentMethod());
            assertEquals(order.getTransactionId(), saved.getTransactionId());
            assertEquals(order.getDeliveryAddress(), saved.getDeliveryAddress());
        } finally {
            del("online_orders", "online_order_id", order.getOnlineOrderId());
        }
    }

    @Test
    void savePersistsOrderItems() throws Exception {
        OnlineOrderDAO dao = new OnlineOrderDAO();
        OnlineOrder order = order("PU-IT-" + System.currentTimeMillis(), "PROCESSING", bd("39.50"), bd("0.50"), List.of(
            item(1, 2, bd("10.00")),
            item(4, 1, bd("20.00"))
        ));
        dao.save(order);

        try {
            OnlineOrder saved = dao.findAll().stream()
                .filter(x -> x.getOnlineOrderId() == order.getOnlineOrderId())
                .findFirst()
                .orElseThrow();

            assertEquals(2, saved.getItems().size());
            assertTrue(saved.getItems().stream().anyMatch(item -> item.getProductId() == 1 && item.getQuantity() == 2));
            assertTrue(saved.getItems().stream().anyMatch(item -> item.getProductId() == 4 && item.getQuantity() == 1));
        } finally {
            del("online_orders", "online_order_id", order.getOnlineOrderId());
        }
    }

    @Test
    void updateStatusPersistsWithoutChangingReferenceOrTransactionId() throws Exception {
        OnlineOrderDAO dao = new OnlineOrderDAO();
        OnlineOrder order = order("PU-IT-" + System.currentTimeMillis(), "RECEIVED", bd("22.10"), BigDecimal.ZERO, List.of(
            item(7, 1, bd("22.10"))
        ));
        dao.save(order);

        try {
            dao.updateStatus(order.getOnlineOrderId(), "DISPATCHED");
            OnlineOrder saved = dao.findAll().stream()
                .filter(x -> x.getOnlineOrderId() == order.getOnlineOrderId())
                .findFirst()
                .orElseThrow();

            assertEquals("DISPATCHED", saved.getStatus());
            assertEquals(order.getOrderReference(), saved.getOrderReference());
            assertEquals(order.getTransactionId(), saved.getTransactionId());
        } finally {
            del("online_orders", "online_order_id", order.getOnlineOrderId());
        }
    }

    private OnlineOrder order(String reference, String status, BigDecimal total, BigDecimal discount, List<OnlineOrderItem> items) {
        OnlineOrder order = new OnlineOrder();
        order.setMerchantId(1);
        order.setMemberId("MEM-" + reference);
        order.setOrderReference(reference);
        order.setDiscountApplied(discount);
        order.setPaymentMethod("ONLINE_CARD");
        order.setTransactionId("TXN-" + reference);
        order.setDeliveryAddress("1 Test Street");
        order.setStatus(status);
        order.setTotalPrice(total);
        order.setItems(items);
        return order;
    }

    private OnlineOrderItem item(int productId, int quantity, BigDecimal unitPrice) {
        OnlineOrderItem item = new OnlineOrderItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        return item;
    }
}