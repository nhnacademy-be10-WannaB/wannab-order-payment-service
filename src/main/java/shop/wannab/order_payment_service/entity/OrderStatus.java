package shop.wannab.order_payment_service.entity;


public enum OrderStatus {
    PENDING, //결제대기
    PAID,   // 결제완료(배송대기)
    SHIPPING, //배송중
    COMPLETED, //완료
    RETURNED, //반품
    CANCELLED, //주문취소
    FAILED; //주문실패
}
