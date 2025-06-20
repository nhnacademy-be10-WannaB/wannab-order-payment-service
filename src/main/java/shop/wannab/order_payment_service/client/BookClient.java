package shop.wannab.order_payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shop.wannab.order_payment_service.entity.dto.OrderBookInfoListDto;
import shop.wannab.order_payment_service.entity.dto.OrderItemListDto;

@FeignClient(name = "wannab-book-service", url = "${book.api.url}") //수정 필요할 수도
public interface BookClient {

    @PostMapping("/api/books/validation/primary")
    void validateOrderItems(@RequestBody OrderItemListDto orderItemListDto);
    @PostMapping("/api/books/for-order")
    OrderBookInfoListDto getOrderBookInfos(@RequestBody OrderItemListDto orderItemListDto);
}

