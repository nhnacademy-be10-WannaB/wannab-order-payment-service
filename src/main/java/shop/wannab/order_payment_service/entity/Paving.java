package shop.wannab.order_payment_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pavings")
public class Paving {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paving_id")
    private Long id;

    @Setter
    @Column(name = "paving_name")
    private String name;

    @Setter
    @NotNull
    @Column(name = "paving_price")
    private int price;

}
