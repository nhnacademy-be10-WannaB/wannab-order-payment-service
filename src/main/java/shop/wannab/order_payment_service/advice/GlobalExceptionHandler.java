package shop.wannab.order_payment_service.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import shop.wannab.order_payment_service.entity.payment.dto.PaymentFailResponseDto;
import shop.wannab.order_payment_service.exception.PaymentProcessingException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<PaymentFailResponseDto> handlePaymentProcessingException(PaymentProcessingException e) {
        return new ResponseEntity<>(e.getFailResponseDto(), e.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<PaymentFailResponseDto> handleGenericException(Exception e) {
        PaymentFailResponseDto errorDto = new PaymentFailResponseDto(
                "INTERNAL_SERVER_ERROR",
                "예상치 못한 서버 오류가 발생했습니다: " + e.getMessage(),
                null,
                null
        );
        return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
