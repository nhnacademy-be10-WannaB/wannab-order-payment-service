package shop.wannab.order_payment_service.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import shop.wannab.order_payment_service.entity.dto.OrderItemValidationError;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class UnavailableOrderBooksException extends RuntimeException {
    private final List<OrderItemValidationError> errors;

}

