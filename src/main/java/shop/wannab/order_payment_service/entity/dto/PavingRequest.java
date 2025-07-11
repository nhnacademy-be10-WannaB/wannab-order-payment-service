package shop.wannab.order_payment_service.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PavingRequest {

    private String name;

    private int price;
}
