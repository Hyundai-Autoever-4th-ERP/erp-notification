package erp.notification.domain;

public enum EventType {
    SO_REQUESTED("주문 승인이 요청됐습니다."),
    SO_APPROVED("주문이 승인됐습니다."),
    SO_REJECTED("주문이 반려됐습니다."),
    SO_SHIPPED("주문 상품이 출고됐습니다."),
    SO_ARRIVED("주문 상품이 도착했습니다."),
    PO_CONFIRMED("구매 주문이 확정됐습니다."),
    PO_RECEIVED("구매 상품이 입고됐습니다."),
    STOCK_LOW("안전재고 기준에 도달했습니다.");

    private final String summary;

    EventType(String summary) {
        this.summary = summary;
    }

    public String summary() {
        return summary;
    }
}
