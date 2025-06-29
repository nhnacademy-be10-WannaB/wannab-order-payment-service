package shop.wannab.order_payment_service.entity.dto;


import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import shop.wannab.order_payment_service.entity.OrderStatus;

//주문 상세내역에서 보여줄 데이터들을 담는 dto
@Data
@AllArgsConstructor
public class OrderDetailResponse {

    private final List<OrderBookDetailResponse> books;

    private final Long orderNumber;          //주문번호 (일단 orderid그대로)
    private final LocalDateTime orderAt;       //주문일시
    private final LocalDateTime paymentAt;     //결제일시
    //private final String deliveryMethod;       //배송방법 (굳이 있어야할까?)
    private final OrderStatus orderStatus;     //주문상태
    //private final String carrierName;          //택배사인데 빼도될거같기도
    private final String deliveryNumber;       //송장번호

    private final int totalPrice;           //총 주문금액


}