package shop.wannab.order_payment_service.entity.payment;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import shop.wannab.order_payment_service.entity.Order;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    private String paymentKey;

    @NotNull
    private String type;

    @NotNull
    private String method;

    @NotNull
    private Integer totalAmount;

    private Integer balanceAmount;

    @NotNull
    private String status;

    @NotNull
    private LocalDateTime requestAt;

    private LocalDateTime approvedAt;

    @OneToOne
    @MapsId
    @JoinColumn(name = "order_id")
    private Order order;
}
