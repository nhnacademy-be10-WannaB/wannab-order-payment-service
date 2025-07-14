package shop.wannab.order_payment_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import shop.wannab.order_payment_service.entity.payment.dto.PaymentFailResponseDto;

@Getter
public class PaymentProcessingException extends RuntimeException {
    private final PaymentFailResponseDto failResponseDto;
    private final HttpStatus httpStatus;

    public PaymentProcessingException(String errorCode, String errorMessage, String orderId, String paymentKey, HttpStatus httpStatus, Throwable cause) {
        super(errorMessage, cause);
        this.failResponseDto = new PaymentFailResponseDto(errorCode, errorMessage, orderId, paymentKey);
        this.httpStatus = httpStatus;
    }

    public PaymentProcessingException(String errorCode, String errorMessage, String orderId, String paymentKey, HttpStatus httpStatus) {
        this(errorCode, errorMessage, orderId, paymentKey, httpStatus, null);
    }
}
