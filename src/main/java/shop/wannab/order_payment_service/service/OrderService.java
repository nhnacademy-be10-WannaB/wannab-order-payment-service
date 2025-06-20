package shop.wannab.order_payment_service.service;

import io.micrometer.common.util.StringUtils;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.order_payment_service.client.BookClient;
import shop.wannab.order_payment_service.client.UserClient;
import shop.wannab.order_payment_service.entity.CartItem;
import shop.wannab.order_payment_service.entity.DeliveryPolicy;
import shop.wannab.order_payment_service.entity.Guest;
import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.entity.OrderBook;
import shop.wannab.order_payment_service.entity.OrderStatus;
import shop.wannab.order_payment_service.entity.WrappingPaper;
import shop.wannab.order_payment_service.entity.dto.*;

import java.util.List;
import shop.wannab.order_payment_service.exception.WrappingPaperNotFoundException;
import shop.wannab.order_payment_service.repository.GuestRepository;
import shop.wannab.order_payment_service.repository.OrderBookRepository;
import shop.wannab.order_payment_service.repository.OrderReopsitory;
import shop.wannab.order_payment_service.repository.WrappingPaperRepository;
import shop.wannab.order_payment_service.service.Impl.EmailService;
import shop.wannab.order_payment_service.service.Impl.OrderEmailHelper;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final DeliveryPolicyService deliveryPolicyService;
    private final UserClient userClient;
    private final BookClient bookClient;
    private final WrappingPaperService wrappingPaperService;

    private final OrderReopsitory orderReopsitory;
    private final GuestRepository guestRepository;
    private final OrderBookRepository orderBookRepository;
    private final WrappingPaperRepository wrappingPaperRepository;
    private final OrderEmailHelper emailHelper;

    public OrderPageRequestDto createOrderPageRequestDto(Long userId, OrderItemListDto orderItemListDto) {
        OrderBookInfoListDto orderBookInfos = bookClient.getOrderBookInfos(orderItemListDto);
        int totalBookPrice = getTotalBookPrice(orderBookInfos);
        int shippingFee = getShippingFee(totalBookPrice);
        int userPoints = 0;
        List<UserAddressResponse> userAddresses = List.of();

        if (userId > 0) {
            userPoints = userClient.getUserPoints(userId, userId);
            userAddresses = userClient.getAllAddresses(userId, userId);
        }

        List<WrappingPaperResponse> wrappingPaperList = wrappingPaperService.getWrappingPaperList();
        //TODO: coupon 정보 추후에 추가
        return new OrderPageRequestDto(orderBookInfos, userAddresses, wrappingPaperList, totalBookPrice, shippingFee, userPoints);
    }

    public int getTotalBookPrice(OrderBookInfoListDto orderBookInfoListDto) {
        int sum = 0;
        List<OrderBookInfo> bookInfos = orderBookInfoListDto.getOrderBookInfos();
        for (OrderBookInfo bookInfo : bookInfos) {
            sum += bookInfo.getSalesPrice() * bookInfo.getQuantity();
        }
        return sum;
    }

    public int getShippingFee(int totalBookPrice) {
        DeliveryPolicy deliveryPolicy = deliveryPolicyService.findApplicablePolicy(totalBookPrice);
        int shippingFee = deliveryPolicy.getFee();
        return shippingFee;
    }


    //주문생성
    @Transactional
    public OrderResponse createOrder(OrderRequest request, Long userId) {
        Order order = new Order();
        order.setOrderAt(ZonedDateTime.now());
        order.setDeliveryWant(request.getDeliveryWant());
        order.setOrderStatus(OrderStatus.PENDING);
        order = orderReopsitory.save(order);



        List<OrderBookRequest> bookList = request.getBookList();
        List<CartItem> itemList = bookList.stream()
                .map(req -> new CartItem(req.getBookId(), req.getQuantity())).toList();

        OrderItemListDto itemListDto = new OrderItemListDto(itemList);

        bookClient.validateOrderItems(itemListDto);
        OrderBookInfoListDto bookInfoList = bookClient.getOrderBookInfos(itemListDto);


        int totalBookPrice = 0; // 도서합계
        int totalWrappingPrice = 0; // 포장지 합계

        for (OrderBookInfo bookInfo : bookInfoList.getOrderBookInfos()) {
            OrderBookRequest match = bookList.stream()
                    .filter(req -> req.getBookId().equals(bookInfo.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("주문서 도서정보 누락"));

            OrderBook orderBook = new OrderBook();
            orderBook.setOrder(order);
            orderBook.setBookId(bookInfo.getId());
            orderBook.setBookPrice(bookInfo.getSalesPrice());
            orderBook.setQuantity(bookInfo.getQuantity());

            int wpPrice = 0;
            WrappingPaper wp = null;
            if(match.getSelectedWrappingId() != null){
                wp = wrappingPaperRepository.findById(match.getSelectedWrappingId()).orElseThrow(()-> new WrappingPaperNotFoundException(match.getSelectedWrappingId()));
                wpPrice = wp.getPrice();
            }
            orderBook.setWrappingPaper(wp);
            orderBook.setWrappingPrice(wpPrice);

            orderBookRepository.save(orderBook);

            totalBookPrice += bookInfo.getSalesPrice() * bookInfo.getQuantity();
            totalWrappingPrice += wpPrice;
        }

        int deliveryFee = getShippingFee(totalBookPrice); //배송비
        order.setTotalBookPrice(totalBookPrice);
        order.setTotalWrappingPrice(totalWrappingPrice);
        order.setDeliveryFee(deliveryFee);


        if (userId > 0) { //회원일시
            int totalDiscount = request.getUsedPoint(); // TODO: 쿠폰 할인 추가
            userClient.usePoint(userId, userId, request.getUsedPoint()); // 사용한 포인트
            order.setUserId(userId);
            order.setAddressId(request.getAddressId());
            order.setTotalDiscount(totalDiscount);

            // 이메일 발송
            emailHelper.sendMemberOrderEmail(userId, order, request.getAddressId());

        } else { //비회원일시
            if (StringUtils.isBlank(request.getName()) ||
                    StringUtils.isBlank(request.getEmail()) ||
                    StringUtils.isBlank(request.getPhone()) ||
                    StringUtils.isBlank(request.getPassword()) ||
                    StringUtils.isBlank(request.getAddress())) {
                throw new IllegalArgumentException("비회원 주문시 모든항목 입력 필수");
            }

            order.setTotalDiscount(0); // 쿠폰, 포인트 사용불가

            Guest guest = new Guest();
            guest.setName(request.getName());
            guest.setEmail(request.getEmail());
            guest.setPhone(request.getPhone());
            guest.setPassword(request.getPassword());
            guest.setAddress(request.getAddress());
            guest.setOrder(order);
            guestRepository.save(guest);

            //이메일 발송
            emailHelper.sendGuestOrderEmail(guest, order);
        }

        orderReopsitory.save(order);

        return new OrderResponse(order.getId(), order.getOrderAt(), order.getTotalPrice());
    }


}
