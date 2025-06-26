package shop.wannab.order_payment_service.entity.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import shop.wannab.order_payment_service.entity.OrderStatus;


//주문목록조회시 클라이언트에게 보여줄 정보 (현재페이지에선 도서정보도 포함하는데 지워도될듯)
@Data
public class OrderLookupResponse {
    private final Long orderId;
    private final String orderName;
    private final LocalDateTime orderAt;    //주문일시
    private final OrderStatus orderStatus;  //배송상태
    private final LocalDate deliveryAt; //출고일
    private final int totalPrice;
}
