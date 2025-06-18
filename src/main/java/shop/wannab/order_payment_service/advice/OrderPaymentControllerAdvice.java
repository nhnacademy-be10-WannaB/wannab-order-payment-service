package shop.wannab.order_payment_service.advice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import shop.wannab.order_payment_service.entity.dto.OrderBookInfo;
import shop.wannab.order_payment_service.exception.UnavailableOrderBooksException;
import shop.wannab.order_payment_service.service.CartService;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class OrderPaymentControllerAdvice {

    private final CartService cartService;

    @ExceptionHandler({UnavailableOrderBooksException.class})
    public ResponseEntity<String> handleUnavailableOrderBooksException(UnavailableOrderBooksException exception) {
        List<OrderBookInfo> invalidBooks = exception.getInvalidBooks();
        StringBuilder sb = new StringBuilder();
        for (OrderBookInfo book : invalidBooks) {
            sb.append(book.getTitle()).append(", ");
            //만약 책이 판매중지되면, 장바구니의 책 삭제
            if (book.getQuantity() == -1) {
                cartService.removeProductFromCart(exception.getUserId(), book.getId());
            }
        }
        sb.setLength(sb.length() - 2);
        sb.append("는 주문 불가합니다(판매중지 또는 재고부족)");
        return new ResponseEntity<>(sb.toString(), HttpStatus.CONFLICT);
    }
}
