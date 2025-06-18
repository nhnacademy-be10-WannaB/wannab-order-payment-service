package shop.wannab.order_payment_service.service;

import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.entity.dto.GuestOrderRequest;
import shop.wannab.order_payment_service.entity.dto.OrderResponse;

public interface GuestOrderService {

    //비회원 주문생성
    OrderResponse createGuestOrder(GuestOrderRequest request);


}
