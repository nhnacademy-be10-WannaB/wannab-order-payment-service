package shop.wannab.order_payment_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "guests")
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guest_id")
    private Long id;


    @Setter
    @NotNull
    @Column(name = "guest_password")
    private String password;

    @Setter
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull
    private Order order;

    public Guest(String password, Order order) {
        this.password = password;
        this.order = order;
    }


}