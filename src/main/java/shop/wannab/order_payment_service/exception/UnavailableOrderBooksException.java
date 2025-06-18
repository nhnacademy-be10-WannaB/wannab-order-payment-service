package shop.wannab.order_payment_service.exception;

import shop.wannab.order_payment_service.entity.dto.OrderBookInfo;

import java.util.List;

public class UnavailableOrderBooksException extends RuntimeException {
    private final List<OrderBookInfo> invalidBooks;
    private final Long userId;
    public UnavailableOrderBooksException(Long userId, List<OrderBookInfo> invalidBooks) {
        //super("One or more books have invalid quantity (<= 0)");
        this.invalidBooks = invalidBooks;
        this.userId = userId;
    }

    public List<OrderBookInfo> getInvalidBooks() {
        return invalidBooks;
    }

    public Long getUserId() {
        return userId;
    }
}

