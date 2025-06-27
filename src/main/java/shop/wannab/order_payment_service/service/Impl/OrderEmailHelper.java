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

    public void sendOrderEmail(Order order, String email, String address, String name) {


        String emailText = String.format("""
                %s님, 주문이 완료되었습니다.

                ▷ 주문번호: %d
                ▷ 결제금액: %,d원
                ▷ 배송주소: %s
                ▷ 배송희망일: %s

                감사합니다.
            """, name, order.getId(), order.getTotalPrice(), address, order.getDeliveryWant());

        emailService.sendOrderEmail(email, "[WannaB] 주문확인서", emailText);
    }

}