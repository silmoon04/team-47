package ie.cortexx.enums;

// debt cycle: NORMAL -> SUSPENDED (overdue) -> IN_DEFAULT (still unpaid after 2nd reminder)
// manager can restore IN_DEFAULT -> NORMAL if payment made
public enum AccountStatus {
    NORMAL, SUSPENDED, IN_DEFAULT
}
