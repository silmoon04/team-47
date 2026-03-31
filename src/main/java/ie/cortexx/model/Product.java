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

    public Product() {}

    // convenience constructor for the common fields needed to create a product
    public Product(String saProductId, String name, BigDecimal costPrice, BigDecimal markupRate) {
        this.saProductId = saProductId;
        this.name = name;
        this.costPrice = costPrice;
        this.markupRate = markupRate;
        this.active = true;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getSaProductId() { return saProductId; }
    public void setSaProductId(String saProductId) { this.saProductId = saProductId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPackageType() { return packageType; }
    public void setPackageType(String packageType) { this.packageType = packageType; }

    public String getUnitType() { return unitType; }
    public void setUnitType(String unitType) { this.unitType = unitType; }

    public int getUnitsPerPack() { return unitsPerPack; }
    public void setUnitsPerPack(int unitsPerPack) { this.unitsPerPack = unitsPerPack; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public BigDecimal getMarkupRate() { return markupRate; }
    public void setMarkupRate(BigDecimal markupRate) { this.markupRate = markupRate; }

    public BigDecimal getVatRate() { return vatRate; }
    public void setVatRate(BigDecimal vatRate) { this.vatRate = vatRate; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getLastSynced() { return lastSynced; }
    public void setLastSynced(LocalDateTime lastSynced) { this.lastSynced = lastSynced; }
}
