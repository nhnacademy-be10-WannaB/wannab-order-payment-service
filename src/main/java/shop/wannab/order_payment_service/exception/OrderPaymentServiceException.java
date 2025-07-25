package shop.wannab.order_payment_service.exception;

import lombok.Getter;

@Getter
public class OrderPaymentServiceException extends BaseException {

    public OrderPaymentServiceException(ErrorCode errorCode) {
        super(errorCode);
    }
}
