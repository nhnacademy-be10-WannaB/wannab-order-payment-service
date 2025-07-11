package shop.wannab.order_payment_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
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
    @Column(name = "paving_price")
    private int price;

}
