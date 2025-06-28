package shop.wannab.order_payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shop.wannab.order_payment_service.entity.dto.OrderBookInfoListDto;
import shop.wannab.order_payment_service.entity.dto.OrderItemListDto;

@FeignClient(name = "book-service")
public interface BookClient {

    @PostMapping("/api/books/validation/primary")
    void validateOrderItems(@RequestBody OrderItemListDto orderItemListDto);

    @PostMapping(value = "/api/books/for-order")
    OrderBookInfoListDto getOrderBookInfos(@RequestBody OrderItemListDto orderItemListDto);
}

