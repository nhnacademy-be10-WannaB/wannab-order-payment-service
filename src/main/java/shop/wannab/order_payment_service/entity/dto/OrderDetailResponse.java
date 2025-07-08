package shop.wannab.order_payment_service.entity.dto;


import java.time.LocalDateTime;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import shop.wannab.order_payment_service.entity.OrderStatus;

//주문 상세내역에서 보여줄 데이터들을 담는 dto
@Data
@AllArgsConstructor
public class OrderDetailResponse {

    private final List<OrderBookDetailResponse> books;
    private final Long orderId;          //주문번호 (일단 orderid그대로)
    private final LocalDateTime orderAt;       //주문일시
    private final OrderStatus orderStatus;     //주문상태
    private final int totalPrice;           //총 주문금액
    private final int shippingFee;  //배송비
    private final int totalDiscount;    //총 할인가격
    private final int totalWrappinpPrice;   //총 포장지가격
    private final String name; //주문자성함 //TODO


}