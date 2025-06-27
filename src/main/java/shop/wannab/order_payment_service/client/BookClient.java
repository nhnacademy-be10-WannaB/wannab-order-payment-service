package shop.wannab.order_payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shop.wannab.order_payment_service.entity.dto.BookIdListDto;
import shop.wannab.order_payment_service.entity.dto.BookIdTitlePriceListDto;
import shop.wannab.order_payment_service.entity.dto.OrderBookInfoListDto;
import shop.wannab.order_payment_service.entity.dto.OrderItemListDto;

@FeignClient(name = "wannab-book-service")
public interface BookClient {

    @PostMapping("/api/books/validation/primary")
    void validateOrderItems(@RequestBody OrderItemListDto orderItemListDto);

    @PostMapping(value = "/api/books/for-order")
    OrderBookInfoListDto getOrderBookInfos(@RequestBody OrderItemListDto orderItemListDto);

    @PostMapping("/api/books/simple-info")
    BookIdTitlePriceListDto getBookSimpleInfos(@RequestBody BookIdListDto bookIdListDto);

    @PostMapping("/decrease-stock")
    ResponseEntity<Void> decreaseStock(@RequestBody OrderItemListDto orderItemListDto);
}

