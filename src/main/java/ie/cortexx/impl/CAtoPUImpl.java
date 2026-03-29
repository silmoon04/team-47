package ie.cortexx.impl;

import ie.cortexx.exception.*;
import ie.cortexx.interfaces.I_CAtoPU;
import ie.cortexx.model.StockItem;
import java.util.List;

// our implementation of the interface Team C calls
// delegates to StockDAO for actual db queries
public class CAtoPUImpl implements I_CAtoPU {

    // TODO: inject StockDAO via constructor

    @Override
    public int getStockLevel(String productId)
            throws IllegalArgumentException, ProductNotFoundException {
        // TODO: throw IllegalArgumentException if productId is null/empty
        // TODO: look up product by sa_product_id via StockDAO/ProductDAO
        // TODO: throw ProductNotFoundException if product doesnt exist
        // TODO: return stock quantity, or 0 if out of stock
        return 0;
    }

    @Override
    public boolean deductStock(String productId, int quantity)
            throws IllegalArgumentException, ProductNotFoundException {
        // TODO: throw IllegalArgumentException if productId null or quantity <= 0
        // TODO: throw ProductNotFoundException if product doesnt exist
        // TODO: check if enough stock, if yes deduct via StockDAO.updateQuantity and return true
        // TODO: if not enough stock return false
        return false;
    }

    @Override
    public List<StockItem> getAllStock()
            throws ServiceUnavailableException {
        // TODO: delegate to StockDAO.findAll()
        // TODO: catch SQLException and wrap in ServiceUnavailableException
        return null;
    }
}
