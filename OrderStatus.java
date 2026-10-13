public enum OrderStatus {
    RECEIVED("прийняте"),
    PREPARING("готується"),
    READY("готове"),
    COMPLETED("завершене");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
