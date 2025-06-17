package shop.wannab.order_payment_service.exception;

public class DeliveryPolicyAlreadyExistsException extends RuntimeException {
    public DeliveryPolicyAlreadyExistsException(String name) {
        super("이미 존재하는 정책입니다 : " + name);
    }
}
