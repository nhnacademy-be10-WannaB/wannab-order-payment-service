package shop.wannab.order_payment_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(name = "order_at")
    private LocalDateTime orderAt;

    @Column(name = "delivery_at")
    private LocalDateTime deliveryAt;

    @Column(name = "delivery_want")
    private LocalDate deliveryWant;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @Column(name = "total_book_price")
    private int totalBookPrice;

    @Column(name = "total_discount")
    private int totalDiscount;

    @Column(name = "delivery_fee")
    private int deliveryFee;

    @Column(name = "total_wrapping_price")
    private int totalWrappingPrice;


    //외부 API
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "adress_id")
    private Long addressId;


    //총 결제금액 계산
    public int getTotalPrice() {
        return totalBookPrice
                + totalWrappingPrice
                + deliveryFee
                - totalDiscount;
    }
}