package shop.wannab.order_payment_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.wannab.order_payment_service.entity.DeliveryPolicy;
import shop.wannab.order_payment_service.entity.dto.OrderBookInfo;
import shop.wannab.order_payment_service.entity.dto.OrderBookInfoListDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final DeliveryPolicyService deliveryPolicyService;

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
