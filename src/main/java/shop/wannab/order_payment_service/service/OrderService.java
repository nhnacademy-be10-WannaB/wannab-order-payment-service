package shop.wannab.order_payment_service.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.order_payment_service.client.BookClient;
import shop.wannab.order_payment_service.client.CouponClient;
import shop.wannab.order_payment_service.client.UserClient;
import shop.wannab.order_payment_service.entity.*;
import shop.wannab.order_payment_service.entity.dto.*;

import shop.wannab.order_payment_service.repository.*;
import shop.wannab.order_payment_service.service.Impl.OrderEmailHelper;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final DeliveryPolicyService deliveryPolicyService;
    private final UserClient userClient;
    private final BookClient bookClient;
    private final PavingService pavingService;
    private final PaymentService paymentService;
    private final CouponClient couponClient;

    private final OrderRepository orderRepository;
    private final GuestRepository guestRepository;
    private final OrderBookRepository orderBookRepository;
    private final PavingRepository pavingRepository;
    private final OrderItemTempRedisRepository orderItemTempRedisRepository;
    private final CouponUsageTempRedisRepository couponUsageTempRedisRepository;
    private final PointHistoryCreateDtoRepository pointHistoryCreateDtoRepository;


    public OrderPageRequestDto createOrderPageRequestDto(Long userId, OrderItemListDto orderItemListDto) {
        log.debug("Order Service : createOrderPageRequestDto");
        OrderBookInfoListDto orderBookInfos = bookClient.getOrderBookInfos(orderItemListDto);
        int totalBookPrice = getTotalBookPrice(orderBookInfos);
        int shippingFee = getShippingFee(totalBookPrice);
        int userPoints = 0;
        List<UserAddressResponse> userAddresses = List.of();
        log.debug("Before PavingService Call");
        List<PavingResponse> pavingList = pavingService.getPavingList();
        log.debug("After PavingService Call");
        if (userId > 0) {
            userPoints = userClient.getUserPoints(userId);
            userAddresses = userClient.getAllAddresses(userId);
            List<Long> bookIdList = orderBookInfos.getOrderBookInfos().stream().map(OrderBookInfo::getBookId).toList();

            ApplicableCouponsDto applicableCouponsDto = couponClient.getApplicableCoupons(userId, new OrderCouponsRequestDto(bookIdList)).getBody();
            List<OrderCouponDto> orderCouponDtos = new ArrayList<>(applicableCouponsDto.getOrderCoupons());
            for (OrderBookInfo orderBookInfo : orderBookInfos.getOrderBookInfos()) {
                long bookId = orderBookInfo.getBookId();
                Map<Long, List<BookCouponDto>> bookIdCouponsMap = applicableCouponsDto.getItemCoupons();
                List<BookCouponDto> bookApplicableCoupons = bookIdCouponsMap.get(bookId);
                orderBookInfo.setApplicableCoupons(bookApplicableCoupons);

            }
            OrderPageRequestDto orderPageRequestDto = new OrderPageRequestDto(orderBookInfos, userAddresses, pavingList, totalBookPrice, shippingFee, userPoints, orderCouponDtos, userId);
            return orderPageRequestDto;
        }
        return new OrderPageRequestDto(orderBookInfos, userAddresses, pavingList, totalBookPrice, shippingFee, userPoints, List.of(), userId);

    }

    //주문생성
    @Transactional
    public OrderInfoForPayment createOrder(OrderSubmitDto orderSubmitDto, Long userId) {
        //------- 재고 2차 검증 -------//
        List<BookOrderSubmitDto> bookList = orderSubmitDto.getBookOrderSubmitDtos();
        List<CartItem> itemList = bookList.stream()
                .map(req -> new CartItem(req.getBookId(), req.getBookQuantity())).toList();

        OrderItemListDto itemListDto = new OrderItemListDto(itemList);
        bookClient.validateOrderItems(itemListDto);

        List<Long> bookIds = orderSubmitDto.getBookOrderSubmitDtos().stream().map(BookOrderSubmitDto::getBookId).toList();
        BookIdTitlePriceListDto bookSimpleInfos = bookClient.getBookSimpleInfos(new BookIdListDto(bookIds));

        Map<Long, Integer> bookIdPriceMap = bookSimpleInfos.getIdTitlePriceDtos().stream()
                .collect(Collectors.toMap(
                        BookIdTitlePriceDto::getBookId,
                        BookIdTitlePriceDto::getSalesPrice
                ));

        //------- Orders table record add -------//
        int totalBookPrice = getTotalBookPrice(bookSimpleInfos, orderSubmitDto.getBookOrderSubmitDtos());
        int totalDiscountAmount = 0;
        if (userId > 0) {
            totalDiscountAmount = getTotalDiscountAmount(userId, orderSubmitDto, bookSimpleInfos, totalBookPrice);
        }
        int shippingFee = getShippingFee(totalBookPrice);
        int totalPavingPrice = getTotalPavingPrice(orderSubmitDto);
        String orderName = createOrderName(bookSimpleInfos.getIdTitlePriceDtos().get(0).getTitle(), bookIds.size());
        Order order = new Order(userId,
                orderName,
//                getShippingDate(),
                null,
                orderSubmitDto.getDeliveryRequestAt(),
                totalBookPrice, totalDiscountAmount,
                shippingFee,
                totalPavingPrice,
                orderSubmitDto.getRecipientName(),
                orderSubmitDto.getEmail(),
                orderSubmitDto.getRecipientPhoneNumber(),
                orderSubmitDto.getRecipientAddress());

        order = orderRepository.save(order);

        //------- Order_book table record add -------//
        List<OrderBook> orderBooks = new ArrayList<>();
        for (BookOrderSubmitDto bookOrderSubmitDto : orderSubmitDto.getBookOrderSubmitDtos()) {
            Paving paving = null;
            if (Objects.nonNull(bookOrderSubmitDto.getPavingId())) {
                paving = pavingRepository.findById(bookOrderSubmitDto.getPavingId()).orElse(null);
            }
            OrderBook orderBook = new OrderBook(order, bookOrderSubmitDto.getBookId(), paving, bookOrderSubmitDto.getBookQuantity(), bookIdPriceMap.get(bookOrderSubmitDto.getBookId()));
            orderBooks.add(orderBook);
        }
        orderBookRepository.saveAll(orderBooks);

        if (userId < 0) {
            Guest guest = new Guest(orderSubmitDto.getGuestPassword(), order);
            guestRepository.save(guest);
        }



        PointHistoryCreateDTO pointHistoryCreateDTO = new PointHistoryCreateDTO(userId,
                                                                                orderSubmitDto.getUsedPoints() == null ? 0 : orderSubmitDto.getUsedPoints(),
                                                                                order.getTotalPrice(),
                                                                                order.getId());
        pointHistoryCreateDtoRepository.save(pointHistoryCreateDTO);

        int payAmount = order.getTotalPrice();

        OrderInfoForPayment orderInfoForPayment = new OrderInfoForPayment(order.getId(), orderName, payAmount);
        return orderInfoForPayment;
    }

    private int getTotalBookPrice(OrderBookInfoListDto orderBookInfoListDto) {
        int totalBookPrice = 0;
        List<OrderBookInfo> bookOrderSubmitDtos = orderBookInfoListDto.getOrderBookInfos();
        for (OrderBookInfo dto : bookOrderSubmitDtos) {
            totalBookPrice += dto.getSalesPrice() * dto.getQuantity();
        }
        return totalBookPrice;
    }

    private int getTotalBookPrice(BookIdTitlePriceListDto orderBookInfoListDto, List<BookOrderSubmitDto> idQuantityEtcDto) {
        int sum = 0;

        Map<Long, Integer> bookIdToQuantityMap = idQuantityEtcDto.stream()
        .collect(Collectors.toMap(
            BookOrderSubmitDto::getBookId,
            BookOrderSubmitDto::getBookQuantity
        )); //TODO: 개선 여지 O

        for (BookIdTitlePriceDto info : orderBookInfoListDto.getIdTitlePriceDtos()) {
            long bookId = info.getBookId();
            int quantity = bookIdToQuantityMap.get(bookId);
            sum += info.getSalesPrice() * quantity;
        }

        return sum;
    }

    //배송지 정책 계산
    private int getShippingFee(int totalBookPrice) {
        DeliveryPolicy deliveryPolicy = deliveryPolicyService.findApplicablePolicy(totalBookPrice);
        int shippingFee = deliveryPolicy.getFee();
        return shippingFee;
    }

    private int getTotalPavingPrice(OrderSubmitDto submitDto) {
        int totalPavingPrice = 0;
        List<BookOrderSubmitDto> bookOrderSubmitDtos = submitDto.getBookOrderSubmitDtos();
        for (BookOrderSubmitDto dto : bookOrderSubmitDtos) {
            if (Objects.isNull(dto.getPavingId()) || dto.getPavingId() == 0L) {
                continue;
            }
            Paving paving = pavingRepository.findById(dto.getPavingId()).orElseThrow(() -> new RuntimeException("잘못된 포장지 아이디"));
            totalPavingPrice += paving.getPrice();
        }
        return totalPavingPrice;
    }

//    private LocalDateTime getShippingDate() { //출고일 정책
//        LocalDateTime now = LocalDateTime.now();
//
//        // 15시 이후면 다음 날로
//        if (now.toLocalTime().isAfter(LocalTime.of(15, 0))) {
//            now = now.plusDays(1);
//        }
//
//        LocalDate date = now.toLocalDate();
//
//        // 주말이면 월요일까지 이동
//        while (date.getDayOfWeek() == DayOfWeek.SATURDAY ||
//               date.getDayOfWeek() == DayOfWeek.SUNDAY) {
//            now = date.plusDays(1).atStartOfDay();
//        }
//        return now;
//    }

    private String createOrderName(String oneOfBookTitle, int orderItemCount) {
        if (orderItemCount > 1) {
            return String.format("%s 주문", oneOfBookTitle);
        }
        return String.format("%s외 %d권 주문", oneOfBookTitle, orderItemCount);
    }

    private int getTotalDiscountAmount(Long userId, OrderSubmitDto dto, BookIdTitlePriceListDto bookIdTitlePriceListDto, int totalBookPrice) {
        int totalDiscountAmount = 0;
        List<BookOrderSubmitDto> bookOrderSubmitDtos = dto.getBookOrderSubmitDtos();
        //책에 적용한 쿠폰의 할인정보 받아오는 로직//
        Map<Long, Long> couponIdBookIdMap = new HashMap<>();
        for (BookOrderSubmitDto bookDto : bookOrderSubmitDtos) {
            if (Objects.nonNull(bookDto.getAppliedCouponId())) {
                couponIdBookIdMap.put(bookDto.getAppliedCouponId(), bookDto.getBookId());
            }
        }

        if (Objects.nonNull(dto.getAppliedOrderCouponId())) {
            couponIdBookIdMap.put(dto.getAppliedOrderCouponId(), null); //주문에 적용할 쿠폰아이디
        }

        List<TryApplyCouponsResponseDto> couponDiscountInfos = couponClient.getApplyCoupons(userId, new TryApplyCouponsRequestDto(couponIdBookIdMap)).getBody();//여기서 책아이디:가격 맵 생성
        Map<Long, Integer> bookIdPriceMap = new HashMap<>();

        for (BookIdTitlePriceDto idTitlePriceDto : bookIdTitlePriceListDto.getIdTitlePriceDtos()) {
            bookIdPriceMap.put(idTitlePriceDto.getBookId(), idTitlePriceDto.getSalesPrice());
        }
        //책에 적용할 쿠폰 할인가 합산
        List<CouponUsageRequestDto.UsedCouponInfo> usedCouponInfos = new ArrayList<>();
        for (TryApplyCouponsResponseDto discountInfo : couponDiscountInfos) {
            Long bookId = discountInfo.getBookId();
            CouponUsageRequestDto.UsedCouponInfo usedCouponInfo = new CouponUsageRequestDto.UsedCouponInfo();
            usedCouponInfo.setBookId(bookId);
            usedCouponInfo.setCouponId(discountInfo.getCouponId());
            usedCouponInfos.add(usedCouponInfo);
            if (Objects.nonNull(bookId)) {//책 각각에 적용
                Integer bookPrice = bookIdPriceMap.get(bookId);
                if (discountInfo.getDiscountType().equals(DiscountType.FIXED)) {
                    totalDiscountAmount += discountInfo.getDiscountValue();
                } else {
                    totalDiscountAmount += (bookPrice * ((double)discountInfo.getDiscountValue() / 100));
                }

            } else { //totalBookPrice에 적용
                if (!Objects.equals(dto.getAppliedOrderCouponId(), discountInfo.getCouponId())) { //early exit
                    continue;
                }
                if (discountInfo.getDiscountType().equals(DiscountType.FIXED)) {
                    totalDiscountAmount += discountInfo.getDiscountValue();
                } else {
                    totalDiscountAmount += (double) totalBookPrice * discountInfo.getDiscountValue() / 100;
                }
            }
        }
        couponUsageTempRedisRepository.saveUsedCouponInfos(userId, usedCouponInfos);

        if (Objects.nonNull(dto.getUserId()) && !dto.getUserId().isBlank()) {
            totalDiscountAmount += dto.getUsedPoints();
        }

        return totalDiscountAmount;
    }

    //주문목록조회 (회원)
    @Transactional(readOnly = true)
    public Page<OrderLookupResponse> getOrdersByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderAt").descending());

        return orderRepository.findAllByUserIdAndOrderStatusNot(userId, OrderStatus.FAILED, pageable)
                .map(order -> {
                    OrderBook orderBook = orderBookRepository.findTop1ByOrder_IdOrderByObIdAsc(order.getId());
                    String thumbnailUrl = null;

                    if (orderBook != null) {
                        // 도서 썸네일 외부에서 받아오기
                        OrderItemListDto oneBook = new OrderItemListDto(
                                List.of(new CartItem(orderBook.getBookId(), orderBook.getQuantity()))
                        );
                        OrderBookInfoListDto bookInfo = bookClient.getOrderBookInfos(oneBook);

                        if (!bookInfo.getOrderBookInfos().isEmpty()) {
                            thumbnailUrl = bookInfo.getOrderBookInfos().get(0).getThumbnailUrl();
                        }
                    }

                    return new OrderLookupResponse(
                            order.getId(),
                            order.getOrderName(),
                            order.getOrderAt(),
                            order.getOrderStatus(),
                            order.getShippedAt(),
                            order.getTotalPrice(),
                            thumbnailUrl // 썸네일 포함해서 DTO 리턴
                    );
                });
    }

    // 쇼핑몰 주문 전체 조회 (관리자용)
    @Transactional(readOnly = true)
    public Page<OrderLookupResponse> getOrders(OrderSearchDto orderSearchDto,
                                               int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderAt").descending());



        return orderRepository.searchOrders(orderSearchDto, pageable);

    }


    //주문상세조회 공통로직
    private OrderDetailResponse orderDetailResponse(Order order) {
        // 주문정보에있는 도서정보를 불러와서 OrderBookDetailResponse dto로 매핑
        List<OrderBook> list = orderBookRepository.findAllByOrder_Id(order.getId());

        // 외부 API에 보낼 bookId + quantity 리스트 만들기
        List<CartItem> items = list.stream()//여기선 중복된 dto로 cartItemDTO를 썼지만.. 주문이 완료된 도서에 대한 bookId와 quantity임
                .map(ob -> new CartItem(ob.getBookId(), ob.getQuantity()))
                .toList();
        OrderItemListDto itemListDto = new OrderItemListDto(items);

        // 외부 도서 서비스에서 도서 상세 정보 받아오기
        OrderBookInfoListDto bookInfos = bookClient.getOrderBookInfos(itemListDto);

        // bookId → OrderBookInfo 매핑 (성능 위해 Map 사용)
        Map<Long, OrderBookInfo> bookInfoMap = bookInfos.getOrderBookInfos().stream()
                .collect(Collectors.toMap(OrderBookInfo::getBookId, info -> info));

        List<OrderBookDetailResponse> bookDetails = list.stream()
                .map(ob -> {
                    OrderBookInfo info = bookInfoMap.get(ob.getBookId());
                    int quantity = ob.getQuantity();
                    int totalPrice = info.getSalesPrice() * quantity;
                    return new OrderBookDetailResponse(
                            info.getBookId(),
                            info.getTitle(),
                            quantity,
                            totalPrice,
                            info.getThumbnailUrl()
                    );
                }).toList();

        return new OrderDetailResponse(
                bookDetails,
                order.getId(),
                order.getOrderAt(),
                order.getOrderStatus(),
                order.getTotalPrice(),
                order.getShippingFee(),
                order.getTotalDiscountAmount(),
                order.getTotalPavingPrice(),
                order.getRecipientName()
        );
    }

    //주문상세조회 (회원)
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("주문정보없음"));

        // 본인만 조회할수있도록
        if (!userId.equals(order.getUserId())) {
            throw new IllegalArgumentException("본인 주문만 반품가능");
        }

        return orderDetailResponse(order);
    }

    //주문상세조회 (비회원)
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderForGuest(Long orderId, String password) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

        Guest guest = guestRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new IllegalArgumentException("비회원 주문자 정보가 없습니다."));

        if (!guest.getPassword().equals(password)) {
            throw new IllegalArgumentException("주문번호 또는 비밀번호가 일치하지 않습니다.");
        }

        return orderDetailResponse(order);
    }

    //주문취소(결제취소) 회원
    @Transactional
    public void cancelOrder(Long orderId, Long userId){

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("주문번호를 찾을수 없음"));

        // 본인만 취소할수있도록 검사
        if (!userId.equals(order.getUserId())) {
            throw new IllegalArgumentException("본인 주문만 취소가능");
        }

        // PAID(대기)에서만 취소가능
        if (order.getOrderStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("현재 주문 상태에서는 취소할 수 없습니다: " + order.getOrderStatus());
        }

        userClient.cancleOrderPointProcess(order.getId());

        order.setOrderStatus(OrderStatus.CANCELLED);

        increaseBookStock(order);

        int cancelMoney = order.getTotalPrice();

        paymentService.paymentCancel(orderId, cancelMoney);
    }


    //주문취소(결제취소) 비회원 -> 비회원은 order에 따로 userId가 없어서 조회하는것처럼 주문번호와 패스워드를 사용해서 주문취소
    @Transactional
    public void cancelGuestOrder(Long orderId, String password){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

        Guest guest = guestRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new IllegalArgumentException("비회원 주문자 정보가 없습니다."));

        if (!guest.getPassword().equals(password)) {
            throw new IllegalArgumentException("주문번호 또는 비밀번호가 일치하지 않습니다.");
        }

        // PAID(대기)에서만 취소가능
        if (order.getOrderStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("현재 주문 상태에서는 취소할 수 없습니다: " + order.getOrderStatus());
        }
        order.setOrderStatus(OrderStatus.CANCELLED);
        increaseBookStock(order);

        int cancelMoney = order.getTotalPrice();

        paymentService.paymentCancel(orderId, cancelMoney);

    }

    private void increaseBookStock(Order order) {
        List<BookIdQuantityProjection> bookIdQuantityProjections = orderBookRepository.queryByOrder(order);
        List<CartItem> orderItems = new ArrayList<>();
        for (BookIdQuantityProjection bookIdQuantityProjection : bookIdQuantityProjections) {
            Long bookId = bookIdQuantityProjection.getBookId();
            Integer quantity = bookIdQuantityProjection.getQuantity();
            CartItem item = new CartItem(bookId, quantity);
            orderItems.add(item);
        }
        OrderItemListDto orderItemListDto = new OrderItemListDto(orderItems);
        bookClient.increaseStock(orderItemListDto);
    }

    //주문상태변경 (관리자)
    @Transactional
    public void updateStatus(Long userId, Long orderId, OrderStatus newStatus){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("주문번호를 찾을수 없음"));

//        // ADMIN 확인
//        String role = userClient.getUserRole(userId);
//
//        if (!"ADMIN".equalsIgnoreCase(role)) {
//            throw new IllegalArgumentException("관리자만 주문 전체 조회 가능");
//        }

        order.setOrderStatus(newStatus);

        if(newStatus.equals(OrderStatus.SHIPPING)){
            order.setShippedAt(LocalDateTime.now());
        }
    }


    //반품 reason부분은 추후에 enum으로 수정

    //반품 (회원)
    @Transactional
    public void refundOrder(Long userId, Long orderId, RefundReason reason){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다"));

        // 본인만 반품할수있도록 검사
        if (!userId.equals(order.getUserId())) {
            throw new IllegalArgumentException("본인 주문만 반품가능");
        }

        // 반품 가능 상태 확인 (예: 배송완료 상태만 반품 허용)
        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new IllegalStateException("현재 상태에서는 반품할 수 없습니다.");
        }


        LocalDateTime deliveryAt = order.getShippedAt(); // 출고일
        int refundPoint = 0;

        if (reason.equals(RefundReason.DAMAGED)) {
            if (deliveryAt == null || deliveryAt.plusDays(30).isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("제품불량은 출고일로부터 30일 이내만 반품이 가능합니다.");
            }
            refundPoint = order.getTotalPrice() + order.getTotalDiscountAmount(); //제품불량은 전부 환불
        } else if (reason.equals(RefundReason.JUST)) {
            if(deliveryAt == null || deliveryAt.plusDays(10).isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("미사용 제품은 출고일로부터 10일 이내만 반품이 가능합니다.");
            }
            refundPoint = order.getTotalBookPrice() + order.getTotalDiscountAmount(); //배송비 제외 환불

        }

        //환불 포인트 값 보내주기
        if (refundPoint > 0) {
            userClient.refundPoint(orderId, refundPoint);
        }

        // 상태 변경
        order.setOrderStatus(OrderStatus.RETURNED);
        increaseBookStock(order);
    }

    //반품(비회원)
    @Transactional
    public void refundGuestOrder(Long orderId, String password, RefundReason reason){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

        Guest guest = guestRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new IllegalArgumentException("비회원 주문자 정보가 없습니다."));

        if (!guest.getPassword().equals(password)) {
            throw new IllegalArgumentException("주문번호 또는 비밀번호가 일치하지 않습니다.");
        }

        int refundMoney = 0;

        // 반품 가능 상태 확인 (예: 배송완료 상태만 반품 허용)
        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new IllegalStateException("현재 상태에서는 반품할 수 없습니다.");
        }
        LocalDateTime shippedAt = order.getShippedAt(); //출고일

        if(reason.equals(RefundReason.DAMAGED)){
            if (shippedAt == null || shippedAt.plusDays(30).isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("제품불량은 출고일로부터 30일 이내만 반품이 가능합니다.");
            }
            refundMoney = order.getTotalPrice(); //제품불량은 전부 환불
        }else if(reason.equals(RefundReason.JUST)){
            if(shippedAt == null || shippedAt.plusDays(10).isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("미사용 제품은 출고일로부터 10일 이내만 반품이 가능합니다.");
            }
            refundMoney = order.getTotalBookPrice(); //단순변심은 배송비제외
        }
        order.setOrderStatus(OrderStatus.RETURNED);
        increaseBookStock(order);


        paymentService.paymentCancel(orderId, refundMoney);

    }

    /**
     * 배송완료체크 (리뷰작성목적)
    */
    public boolean isReviewable(Long orderBookId) {
        return orderBookRepository.existsByObIdAndOrder_OrderStatus(
                orderBookId, OrderStatus.COMPLETED
        );
    }

    public void saveTemporaryOrderInfo(long customerId, OrderItemListDto dto) {
        orderItemTempRedisRepository.saveTemporaryOrderInfo(customerId, dto);
    }

    public OrderItemListDto consumeTemporaryOrderInfo(long customerId) {
        List<CartItem> orderItems = orderItemTempRedisRepository.getOrderItems(customerId);
        return new OrderItemListDto(orderItems);
    }

}
