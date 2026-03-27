package ie.cortexx.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// maps to `products` table
// demo: 14 products, 100% markup, 0% vat
public class Product {
    private int productId;
    // VARCHAR in schema, ids like '100 00001'
    private String saProductId;
    private String name;
    private String description;
    // e.g. paracetamol: Box, Caps, 20
    private String packageType;
    private String unitType;
    private int unitsPerPack;
    private BigDecimal costPrice;
    // 1.0000 = 100% markup, retail = cost * (1 + markup)
    private BigDecimal markupRate;
    // 0.0000 for demo
    private BigDecimal vatRate;
    private String category;
    private boolean active;
    private LocalDateTime lastSynced;

    // TODO: generate getters & setters
}
