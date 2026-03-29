package ie.cortexx.interfaces;

import ie.cortexx.exception.*;
import ie.cortexx.model.StockItem;
import java.util.List;

// interface WE provide to Team C (PU subsystem, team 48)
// they call OUR methods to check/deduct stock
// must match HLD section 7.5.2
public interface I_CAtoPU {

    int getStockLevel(String productId)
        throws IllegalArgumentException, ProductNotFoundException;

    boolean deductStock(String productId, int quantity)
        throws IllegalArgumentException, ProductNotFoundException;

    List<StockItem> getAllStock()
        throws ServiceUnavailableException;
}
