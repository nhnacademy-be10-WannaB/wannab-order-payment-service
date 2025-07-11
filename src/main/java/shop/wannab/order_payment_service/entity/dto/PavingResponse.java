package shop.wannab.order_payment_service.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PavingResponse {

    private Long id;
    private String name;
    private int price;
}
