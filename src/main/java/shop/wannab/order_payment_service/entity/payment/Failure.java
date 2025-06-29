package shop.wannab.order_payment_service.entity.payment;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Failure {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "payment_key")
    private Payment payment;

    @NotNull
    private String errorCode;

    @NotNull
    private String message;
}
