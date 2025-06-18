package shop.wannab.order_payment_service.entity;



public enum OrderStatus {
    PENDING, //대기
    SHIPPING, //배송중
    COMPLETED, //완료
    RETURNED, //반품
    CANCELLED; //주문취소
}
