package shop.wannab.order_payment_service.entity.dto;


import lombok.Data;

//TODO:임시 (도서api에서 값을 받아옴)
@Data
public class BookDto {
    private Long id;
    private String title;
    private int price;
    private String author;
}