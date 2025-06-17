package shop.wannab.order_payment_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "delivery_policy")
public class DeliveryPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dp_id")
    private Long id;

    @Setter
    @Column(name = "dp_name", unique = true)
    @NotNull
    private String name;

    @Setter
    @Column(name = "dp_fee")
    @NotNull
    private int fee;

    @Setter
    @Column(name = "dp_min")
    @NotNull
    private int minPrice;


    //배송정책 계산로직 (우선 배송정책쪽에 넣었는데 나중에 바꿀필요가 있으면 수정필요)
    public boolean isApplicable(int orderPrice) {
        return orderPrice >= this.minPrice;
    }
}
