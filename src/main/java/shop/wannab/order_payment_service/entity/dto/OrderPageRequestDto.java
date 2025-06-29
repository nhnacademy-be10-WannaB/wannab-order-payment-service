package shop.wannab.order_payment_service.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class OrderPageRequestDto {
    private OrderBookInfoListDto orderBookInfoListDto;
    private List<UserAddressResponse> userAddressList;
    private List<WrappingPaperResponse> wrappingPaperList;
    private int totalBookPrice;
    private int shippingFee;
    private int userPoints;
    //TODO: coupon정보들..
}
