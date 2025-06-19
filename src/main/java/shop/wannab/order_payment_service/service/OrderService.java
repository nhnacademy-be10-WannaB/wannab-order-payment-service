package shop.wannab.order_payment_service.service;

import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.entity.dto.OrderRequest;
import shop.wannab.order_payment_service.entity.dto.OrderResponse;

public interface OrderService {

    //주문생성
    OrderResponse createOrder(OrderRequest request, Long userId);


}
