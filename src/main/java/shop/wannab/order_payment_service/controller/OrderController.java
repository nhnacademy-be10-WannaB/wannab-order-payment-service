package shop.wannab.order_payment_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import shop.wannab.order_payment_service.client.BookClient;
import shop.wannab.order_payment_service.client.UserClient;
import shop.wannab.order_payment_service.entity.dto.*;
import shop.wannab.order_payment_service.service.OrderService;
import shop.wannab.order_payment_service.service.WrappingPaperService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final UserClient userClient;
    private final BookClient bookClient;
    private final OrderService orderService;
    private final WrappingPaperService wrappingPaperService;


    @PostMapping
    public OrderPageRequestDto getNecesaryOrderInfo(@RequestHeader("X-User-Id") Long userId, @RequestBody OrderItemListDto orderItemListDto) throws JsonProcessingException {
        bookClient.validateOrderItems(orderItemListDto); //프론트까지 에러 전달

        OrderBookInfoListDto orderBookInfos = bookClient.getOrderBookInfos(orderItemListDto);
        int totalBookPrice = orderService.getTotalBookPrice(orderBookInfos);    // 총 도서 금액
        int shippingFee = orderService.getShippingFee(totalBookPrice);          // 배송비
        int userPoints = userClient.getUserPoints(userId, userId);                      //가용할 수 있는 유저포인트
        List<UserAddressResponse> userAddresses = userClient.getAllAddresses(userId);
        List<WrappingPaperResponse> wrappingPaperList = wrappingPaperService.getWrappingPaperList(); //포장지 리스트
        //TODO: 유저아이디, 책목록으로 쿠폰쪽에 rest request보내서, 사용할 수 있는 쿠폰리스트 받기

        OrderPageRequestDto orderPageRequestDto = new OrderPageRequestDto(orderBookInfos, userAddresses, wrappingPaperList, totalBookPrice, shippingFee, userPoints);
        return orderPageRequestDto;
    }
}
