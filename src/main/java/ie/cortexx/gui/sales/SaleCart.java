package ie.cortexx.gui.sales;

import java.util.ArrayList;
import java.util.List;

// holds cart state + calc logic, separate from swing wiring
final class SaleCart {
    private final List<Item> items = new ArrayList<>();

    void addItem(String name, double unitPrice) {
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (item.name().equals(name)) {
                items.set(i, item.withQuantity(item.quantity() + 1));
                return;
            }
        }
        items.add(new Item(name, unitPrice, 1));
    }

    void updateQuantity(String name, int delta) {
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (!item.name().equals(name)) {
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

    void removeItem(String name) {
        items.removeIf(item -> item.name().equals(name));
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

    boolean isEmpty() {
        return items.isEmpty();
    }

    Totals totals(double discountRate) {
        double subtotal = items.stream().mapToDouble(Item::lineTotal).sum();
        double discount = subtotal * discountRate;
        double total = subtotal - discount;
        return new Totals(subtotal, discount, total);
    }

    record Item(String name, double unitPrice, int quantity) {
        Item withQuantity(int nextQuantity) {
            return new Item(name, unitPrice, nextQuantity);
        }

        double lineTotal() {
            return unitPrice * quantity;
        }
    }

    record Totals(double subtotal, double discount, double total) {
    }
}
