package ie.cortexx.enums;

// CASH/DEBIT_CARD/CREDIT_CARD for walk-in sales (scenario 11)
// ON_CREDIT when account holder buys on credit (scenarios 10, 12, 13)
// ACCOUNT_PAYMENT when account holder pays off balance (scenario 14)
public enum PaymentType {
    CASH, DEBIT_CARD, CREDIT_CARD, ON_CREDIT, ACCOUNT_PAYMENT
}
