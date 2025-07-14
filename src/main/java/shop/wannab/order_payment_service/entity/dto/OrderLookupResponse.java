package shop.wannab.order_payment_service.entity.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shop.wannab.order_payment_service.entity.OrderStatus;


//주문목록조회시 클라이언트에게 보여줄 정보 (현재페이지에선 도서정보도 포함하는데 지워도될듯)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLookupResponse {
    private Long orderId;
    private String orderName;
    private LocalDateTime orderAt;    //주문일시
    private OrderStatus orderStatus;  //배송상태
    private LocalDateTime shippedAt; //출고일
    private int totalPrice;
    private String thumbnailUrl;

    public OrderLookupResponse(Long orderId, String orderName, LocalDateTime orderAt,
                               OrderStatus orderStatus, LocalDateTime shippedAt, int totalPrice) {
        this.orderId = orderId;
        this.orderName = orderName;
        this.orderAt = orderAt;
        this.orderStatus = orderStatus;
        this.shippedAt = shippedAt;
        this.totalPrice = totalPrice;
    }

}




