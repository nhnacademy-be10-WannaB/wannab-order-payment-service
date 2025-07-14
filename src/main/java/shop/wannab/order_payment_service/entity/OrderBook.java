package shop.wannab.order_payment_service.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "order_book")
public class OrderBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ob_id")
    private Long obId;

    @Column(name = "ob_quantity")
    private Integer quantity;

    @NotNull
    @Column(name = "book_price")
    private int bookPrice;

    @NotNull
    @Column(name = "paving_price")
    private int pavingPrice = 0;


    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    //외부 api
    @NotNull
    @Column(name = "book_id")
    private Long bookId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paving_id")
    private Paving paving;

    public OrderBook(Order order, long bookId, Paving paving, int quantity, int pricePerBook) {
        this.order = order;
        this.bookId = bookId;
        this.paving = paving;
        this.quantity = quantity;
        this.bookPrice = pricePerBook;
        if (Objects.nonNull(this.paving)) {
            this.pavingPrice = paving.getPrice();
        }

    }
}