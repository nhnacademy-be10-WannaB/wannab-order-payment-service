package shop.wannab.order_payment_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.wannab.order_payment_service.client.BookClient;
import shop.wannab.order_payment_service.client.UserClient;
import shop.wannab.order_payment_service.entity.DeliveryPolicy;
import shop.wannab.order_payment_service.entity.dto.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final DeliveryPolicyService deliveryPolicyService;
    private final UserClient userClient;
    private final BookClient bookClient;
    private final WrappingPaperService wrappingPaperService;

    public OrderPageRequestDto createOrderPageRequestDto(Long userId, OrderItemListDto orderItemListDto) {
        OrderBookInfoListDto orderBookInfos = bookClient.getOrderBookInfos(orderItemListDto);
        int totalBookPrice = getTotalBookPrice(orderBookInfos);
        int shippingFee = getShippingFee(totalBookPrice);
        int userPoints = 0;
        List<UserAddressResponse> userAddresses = List.of();

        if (userId > 0) {
            userPoints = userClient.getUserPoints(userId, userId);
            userAddresses = userClient.getAllAddresses(userId);
        }

        List<WrappingPaperResponse> wrappingPaperList = wrappingPaperService.getWrappingPaperList();
        //TODO: coupon 정보 추후에 추가
        return new OrderPageRequestDto(orderBookInfos, userAddresses, wrappingPaperList, totalBookPrice, shippingFee, userPoints);
    }

    public int getTotalBookPrice(OrderBookInfoListDto orderBookInfoListDto) {
        int sum = 0;
        List<OrderBookInfo> bookInfos = orderBookInfoListDto.getOrderBookInfos();
        for (OrderBookInfo bookInfo : bookInfos) {
            sum += bookInfo.getSalesPrice() * bookInfo.getQuantity();
        }
        return sum;
    }

    public int getShippingFee(int totalBookPrice) {
        DeliveryPolicy deliveryPolicy = deliveryPolicyService.findApplicablePolicy(totalBookPrice);
        int shippingFee = deliveryPolicy.getFee();
        return shippingFee;
    }
}
