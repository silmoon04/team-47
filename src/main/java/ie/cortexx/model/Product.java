package ie.cortexx.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// maps to `products` table
// all money fields use BigDecimal not double
public class Product {
    private int productId;
    private String saProductId;
    private String name;
    private BigDecimal costPrice;
    private BigDecimal markupRate;
    private BigDecimal vatRate;
    private String category;
    private boolean active;
    private LocalDateTime lastSynced;

    // TODO: generate getters & setters
}