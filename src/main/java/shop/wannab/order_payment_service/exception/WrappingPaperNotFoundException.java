package shop.wannab.order_payment_service.exception;

public class WrappingPaperNotFoundException extends RuntimeException {
    public WrappingPaperNotFoundException(Long id) {
        super("포장지를 찾을 수 없습니다: " + id);
    }
}
