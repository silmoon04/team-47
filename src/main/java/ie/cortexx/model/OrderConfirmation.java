package ie.cortexx.model;

// not stored in DB, returned by team A when we place an order
public class OrderConfirmation {

    private String saOrderId;
    private String status;
    private String message;

    public OrderConfirmation() {}

    public OrderConfirmation(String saOrderId, String status, String message) {
        this.saOrderId = saOrderId;
        this.status = status;
        this.message = message;
    }

    public String getSaOrderId() { return saOrderId; }
    public void setSaOrderId(String saOrderId) { this.saOrderId = saOrderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
