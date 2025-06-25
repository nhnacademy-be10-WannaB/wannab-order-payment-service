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

import jakarta.validation.constraints.NotNull;
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

    @Column(name = "shipped_at")
    private LocalDate shippedAt;

    @Column(name = "delivery_want")
    private LocalDate deliveryWant;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @Column(name = "total_book_price")
    private int totalBookPrice;

    @Column(name = "total_discount_amount")
    private int totalDiscountAmount;

    @Column(name = "shipping_fee")
    private int shippingFee;

    @Column(name = "total_wrapping_price")
    private int totalWrappingPrice;

    @Column(name = "user_id")
    private Long userId;

    @Setter
    @NotNull
    @Column(name = "recipient_name")
    private String recipientName;

    @Setter
    @NotNull
    @Column(name = "recipient_email")
    private String recipientEmail;

    @Setter
    @NotNull
    @Column(name = "recipient_phone")
    private String recipientPhoneNumber;

    @Setter
    @NotNull
    @Column(name = "recipient_address")
    private String recipientAddress;


    public Order(Long userId, LocalDate shippedAt, LocalDate deliveryRequestAt, int totalBookPrice, int totalDiscountAmount, int shippingFee, int totalWrappingPaperPrice, String recipientName, String recipientEmail, String recipientPhoneNumber, String recipientAddress) {
        this.orderAt =  LocalDateTime.now();
        this.shippedAt = shippedAt;
        this.orderStatus = OrderStatus.PENDING;
        this.deliveryWant = deliveryRequestAt;
        this.userId = userId;
        this.totalBookPrice = totalBookPrice;
        this.totalDiscountAmount = totalDiscountAmount;
        this.shippingFee = shippingFee;
        this.totalWrappingPrice = totalWrappingPaperPrice;
        this.recipientName = recipientName;
        this.recipientEmail = recipientEmail;
        this.recipientPhoneNumber = recipientPhoneNumber;
        this.recipientAddress = recipientAddress;
    }

    //총 결제금액 계산
    public int getTotalPrice() {
        return totalBookPrice
                + totalWrappingPrice
                + shippingFee
                - totalDiscountAmount;
    }
}