package ie.cortexx.gui.sales;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

// holds cart state + calc logic, separate from swing wiring
final class SaleCart {
    private final List<Item> items = new ArrayList<>();

    void addItem(int productId, String name, BigDecimal unitPrice) {
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (item.productId() == productId) {
                items.set(i, item.withQuantity(item.quantity() + 1));
                return;
            }
        }
        items.add(new Item(productId, name, unitPrice, 1));
    }

    void updateQuantity(int productId, int delta) {
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (item.productId() != productId) {
                continue;
            }

            int nextQuantity = item.quantity() + delta;
            if (nextQuantity <= 0) {
                items.remove(i);
            } else {
                items.set(i, item.withQuantity(nextQuantity));
            }
            return;
        }
    }

    void removeItem(int productId) {
        items.removeIf(item -> item.productId() == productId);
    }

    void clear() {
        items.clear();
    }

    List<Item> items() {
        return List.copyOf(items);
    }

    int itemCount() {
        return items.size();
    }

    int quantityOf(int productId) {
        return items.stream()
            .filter(item -> item.productId() == productId)
            .mapToInt(Item::quantity)
            .findFirst()
            .orElse(0);
    }

    boolean isEmpty() {
        return items.isEmpty();
    }

    Totals totals(BigDecimal discountRate) {
        BigDecimal subtotal = items.stream()
            .map(Item::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = subtotal.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.subtract(discount);
        return new Totals(subtotal, discount, total);
    }

    record Item(int productId, String name, BigDecimal unitPrice, int quantity) {
        Item withQuantity(int nextQuantity) {
            return new Item(productId, name, unitPrice, nextQuantity);
        }

        BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    record Totals(BigDecimal subtotal, BigDecimal discount, BigDecimal total) {
    }
}
