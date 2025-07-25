package shop.wannab.order_payment_service.exception;

public interface ErrorCode {
    int getStatus();

    String getCode();

    String getMessage();
}
