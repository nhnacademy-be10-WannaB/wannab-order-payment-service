package shop.wannab.order_payment_service.entity.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderInfoForPayment {
    private Long orderId; //주문번호
    private String orderName;
    private int payAmount;
}
