package shop.wannab.order_payment_service.exception;

public class DeliveryPolicyNotFoundException extends RuntimeException {
    public DeliveryPolicyNotFoundException(Long id) {
        super("배송비 정책을 찾을 수 없습니다 : " + id);
    }
}
