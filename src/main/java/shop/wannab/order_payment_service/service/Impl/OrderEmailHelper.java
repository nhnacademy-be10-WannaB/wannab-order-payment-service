package shop.wannab.order_payment_service.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.wannab.order_payment_service.client.UserClient;
import shop.wannab.order_payment_service.entity.Guest;
import shop.wannab.order_payment_service.entity.Order;

@Service
@RequiredArgsConstructor
public class OrderEmailHelper {

    private final EmailService emailService;
    private final UserClient userClient;

    public void sendMemberOrderEmail(Long userId, Order order, String userAddress) {


        String emailText = String.format("""
                회원님, 주문이 완료되었습니다.

                ▷ 주문번호: %d
                ▷ 할인금액: %,d원
                ▷ 결제금액: %,d원
                ▷ 배송주소: %s
                ▷ 배송희망일: %s

                감사합니다.
            """, order.getId(), order.getTotalDiscountAmount(), order.getTotalPrice(), userAddress, order.getDeliveryWant());

        String email = userClient.getUserEmail(userId);
        emailService.sendOrderEmail(email, "[WannaB] 회원 주문확인서", emailText);
    }


}