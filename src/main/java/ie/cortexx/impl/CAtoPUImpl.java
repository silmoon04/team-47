package ie.cortexx.impl;

import ie.cortexx.interfaces.I_CAtoPU;
import ie.cortexx.model.StockItem;
import java.util.List;

// our implementation of the interface Team C calls
// basically just delegates to StockDAO
public class CAtoPUImpl implements I_CAtoPU {

    // TODO: inject StockDAO or StockService

    @Override
    public int getStockLevel(String productId) {
        // TODO: look up stock for this product
        return 0;
    }

    @Override
    public boolean deductStock(String productId, int quantity) {
        // TODO: try to deduct, return false if not enough
        return false;
    }

    @Override
    public List<StockItem> getAllStock() {
        // TODO: return all stock items
        return null;
    }
}