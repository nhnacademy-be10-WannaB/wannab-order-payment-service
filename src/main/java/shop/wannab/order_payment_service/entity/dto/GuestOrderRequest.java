package shop.wannab.order_payment_service.entity.dto;


import lombok.Data;

/**
 * 게스트 주문조회 및 반품,취소 시 필요
 */
@Data
public class GuestOrderRequest {
    private Long orderId;
    private String password;
}