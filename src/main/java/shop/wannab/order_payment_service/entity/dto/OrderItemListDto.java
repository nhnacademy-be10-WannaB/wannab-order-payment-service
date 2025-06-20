package shop.wannab.order_payment_service.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.wannab.order_payment_service.entity.CartItem;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OrderItemListDto {
    private List<CartItem> orderItems;
}
