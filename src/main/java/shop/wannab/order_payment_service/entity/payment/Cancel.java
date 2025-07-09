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

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Cancel {
    @Id
    private String id; // 👈 Long에서 String으로 변경

    @OneToOne
    @MapsId // 👈 이 어노테이션이 id 필드에 payment의 ID(paymentKey)를 매핑해 줌
    @JoinColumn(name = "payment_key")
    private Payment payment;

    @NotNull
    private Integer cancelAmount;

    @NotNull
    private LocalDateTime cancelAt;

    // id를 제외한 필드만 받는 생성자
    public Cancel(Payment payment, Integer cancelAmount, LocalDateTime cancelAt) {
        this.payment = payment;
        this.cancelAmount = cancelAmount;
        this.cancelAt = cancelAt;
    }
}
