package shop.wannab.order_payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import shop.wannab.order_payment_service.entity.dto.BookDto;

//TODO:임시
@FeignClient(name = "book-service", url = "localhost:8080")
public interface BookFeignClient {

    @GetMapping("/{id}")
    BookDto getBook(@PathVariable("id") Long bookId);
}