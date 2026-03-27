package ie.cortexx.enums;

// PACKED was missing from the original, schema has it between PROCESSING and DISPATCHED
public enum OrderStatus {
    ACCEPTED, PROCESSING, PACKED, DISPATCHED, DELIVERED, CANCELLED
}
