package ie.cortexx.interfaces;

import ie.cortexx.model.StockItem;
import java.util.List;

// interface WE provide to Team C (PU subsystem)
// they call OUR methods to check/deduct stock
public interface I_CAtoPU {
    int getStockLevel(String productId);
    boolean deductStock(String productId, int quantity);
    List<StockItem> getAllStock();
}