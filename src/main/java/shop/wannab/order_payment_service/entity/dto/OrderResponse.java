package shop.wannab.order_payment_service.entity.dto;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

//결제하기를 눌렀을때 주문이 생성되고 생성된 값을 클라이언트에게 보여줌
@Data
@AllArgsConstructor
public class OrderResponse {
    private final Long orderId; //주문번호
    private final LocalDateTime orderAt; //주문일시
    private final int totalPrice; //주문금액(결제금액)
}