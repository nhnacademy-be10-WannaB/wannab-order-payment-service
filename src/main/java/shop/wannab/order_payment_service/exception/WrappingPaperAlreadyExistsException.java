package shop.wannab.order_payment_service.exception;

public class WrappingPaperAlreadyExistsException extends RuntimeException {
    public WrappingPaperAlreadyExistsException(String name) {
        super("이미 존재하는 포장지 입니다: " + name);
    }
}
