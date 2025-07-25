package shop.wannab.order_payment_service.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderPaymentErrorCode implements ErrorCode {

    CART_BAD_REQUEST(400, "ORDER-PAYMENT-8000", "카트 사용자를 식별할 수 없습니다."),
    WRONG_BOOK_ID(404, "ORDER-PAYMENT-8001", "잘못된 도서정보입니다."),
    NO_DEFAULT_DELIVERY_POLICY(500, "ORDER-PAYMENT-8002", "배송비 기본 정책이 설정되지 않았습니다."),
    USER_NOT_FOUND(404, "ORDER-PAYMENT-8003", "유저를 찾을 수 없습니다."),
    NOT_FOUND_ORDER_INFO(404, "ORDER-PAYMENT-8004", "주문 정보를 찾을 수 없습니다."),
    YOU_CAN_ONLY_ACCESS_YOUR_ORDER(403, "ORDER-PAYMENT-8005", "다른 사람의 주문정보에 접근할 수 없습니다"),
    ONLY_CAN_CANCEL_WHEN_ORDER_PAID(409, "ORDER-PAYMENT-8006", "현재 주문 상태에서는 취소할 수 없습니다"),
    ONLY_CAN_REFUND_WHEN_ORDER_COMPLETED(409, "ORDER-PAYMENT-8007", "현재 주문 상태에서는 환불할 수 없습니다."),
    DAMAGED_ITEM_REFUND_RESTRICTION(409, "ORDER-PAYMENT-8", "제품불량은 출고일로부터 30일 이내만 반품이 가능합니다."),
    UNUSED_ITEM_REFUND_RESTRICTION(409, "ORDER-PAYMENT-9", "미사용 제품은 출고일로부터 10일 이내만 반품이 가능합니다."),
    PAVING_ALREADY_EXISTS(409, "ORDER-PAYMENT-10", "이미 존재하는 포장지 이름입니다."),
    PAVING_NOT_EXISTS(404, "ORDER-PAYMENT-11", "존재하지 않는 포장지입니다."),
    ;

    private final int status;
    private final String code;
    private final String message;
}
