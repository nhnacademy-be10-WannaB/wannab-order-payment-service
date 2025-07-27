package shop.wannab.order_payment_service.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import shop.wannab.order_payment_service.entity.dto.CouponUsageRequestDto.UsedCouponInfo;
import shop.wannab.order_payment_service.exception.OrderPaymentErrorCode;
import shop.wannab.order_payment_service.exception.OrderPaymentServiceException;
import shop.wannab.order_payment_service.repository.*;


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
        log.info("action=createOrderPageRequestDto, userId={}, initialItemCount={}, message=\"주문 페이지 요청 DTO 생성 로직 시작\"",
                userId, orderItemListDto.getOrderItems().size());

        OrderBookInfoListDto orderBookInfos = bookClient.getOrderBookInfos(orderItemListDto);
        log.info("action=createOrderPageRequestDto, message=\"도서 정보 클라이언트 호출 완료, 조회된 도서 수: {}\"",
                orderBookInfos.getOrderBookInfos().size());

        int totalBookPrice = getTotalBookPrice(orderBookInfos);
        int shippingFee = getShippingFee(totalBookPrice);
        log.info("action=createOrderPageRequestDto, totalBookPrice={}, shippingFee={}, message=\"총 도서 가격 및 배송비 계산 완료\"",
                totalBookPrice, shippingFee);

        int userPoints = 0;
        List<UserAddressResponse> userAddresses = List.of();
        List<PavingResponse> pavingList = pavingService.getPavingList();

        if (userId > 0) {
            log.info("action=createOrderPageRequestDto, userId={}, message=\"로그인 사용자 로직 시작\"", userId);
            userPoints = userClient.getUserPoints(userId);
            userAddresses = userClient.getAllAddresses(userId);
            log.info(
                    "action=createOrderPageRequestDto, userId={}, userPoints={}, userAddressCount={}, message=\"사용자 포인트 및 주소 정보 조회 완료\"",
                    userId, userPoints, userAddresses.size());

            List<Long> bookIdList = orderBookInfos.getOrderBookInfos().stream().map(OrderBookInfo::getBookId).toList();
            log.info("action=createOrderPageRequestDto, userId={}, bookIdListSize={}, message=\"적용 가능한 쿠폰 조회 시작\"",
                    userId, bookIdList.size());

            ApplicableCouponsDto applicableCouponsDto = couponClient.getApplicableCoupons(userId,
                    new OrderCouponsRequestDto(bookIdList)).getBody();
            log.info(
                    "action=createOrderPageRequestDto, userId={}, applicableCouponsCount={}, message=\"적용 가능한 쿠폰 조회 완료\"",
                    userId,
                    applicableCouponsDto != null ? applicableCouponsDto.getOrderCoupons().size() : 0);

            List<OrderCouponDto> orderCouponDtos = new ArrayList<>(
                    applicableCouponsDto != null ? applicableCouponsDto.getOrderCoupons() : null);

            for (OrderBookInfo orderBookInfo : orderBookInfos.getOrderBookInfos()) {
                long bookId = orderBookInfo.getBookId();
                Map<Long, List<BookCouponDto>> bookIdCouponsMap = applicableCouponsDto.getItemCoupons();
                List<BookCouponDto> bookApplicableCoupons = bookIdCouponsMap.get(bookId);
                orderBookInfo.setApplicableCoupons(bookApplicableCoupons);
                log.debug(
                        "action=createOrderPageRequestDto, bookId={}, applicableBookCouponsCount={}, message=\"도서별 쿠폰 적용 완료\"",
                        bookId, bookApplicableCoupons != null ? bookApplicableCoupons.size() : 0);

            }
            log.info("action=createOrderPageRequestDto, userId={}, message=\"모든 도서에 쿠폰 정보 매핑 완료\"", userId);

            OrderPageRequestDto orderPageRequestDto = new OrderPageRequestDto(orderBookInfos, userAddresses, pavingList,
                    totalBookPrice, shippingFee, userPoints, orderCouponDtos, userId);
            log.info("action=createOrderPageRequestDto, userId={}, message=\"로그인 사용자용 주문 페이지 요청 DTO 생성 완료\"", userId);

            return orderPageRequestDto;
        }
        return new OrderPageRequestDto(orderBookInfos, userAddresses, pavingList, totalBookPrice, shippingFee,
                userPoints, List.of(), userId);

    }

    //주문생성
    @Transactional
    public OrderInfoForPayment createOrder(OrderSubmitDto orderSubmitDto, Long userId) {
        log.info("action=createOrder, userId={}, totalBookCount={}, message=\"주문 생성 로직 시작\"", userId,
                orderSubmitDto.getBookOrderSubmitDtos().size());

        //------- 재고 2차 검증 -------//
        List<BookOrderSubmitDto> bookList = orderSubmitDto.getBookOrderSubmitDtos();
        List<CartItem> itemList = bookList.stream()
                .map(req -> new CartItem(req.getBookId(), req.getBookQuantity())).toList();

        OrderItemListDto itemListDto = new OrderItemListDto(itemList);

        log.info("action=createOrder, message=\"도서 재고 2차 검증 시작\"");
        bookClient.validateOrderItems(itemListDto);
        log.info("action=createOrder, message=\"도서 재고 2차 검증 완료\"");

        List<Long> bookIds = orderSubmitDto.getBookOrderSubmitDtos().stream().map(BookOrderSubmitDto::getBookId)
                .toList();
        BookIdTitlePriceListDto bookSimpleInfos = bookClient.getBookSimpleInfos(new BookIdListDto(bookIds));
        log.info("action=createOrder, retrievedBookCount={}, message=\"도서 간략 정보 조회 완료\"",
                bookSimpleInfos.getIdTitlePriceDtos().size());

        Map<Long, Integer> bookIdPriceMap = bookSimpleInfos.getIdTitlePriceDtos().stream()
                .collect(Collectors.toMap(
                        BookIdTitlePriceDto::getBookId,
                        BookIdTitlePriceDto::getSalesPrice
                ));
        log.debug("action=createOrder, message=\"도서 ID-가격 맵 생성 완료\""); // 상세 과정이므로 debug

        //------- Orders table record add -------//
        int totalBookPrice = getTotalBookPrice(bookSimpleInfos, orderSubmitDto.getBookOrderSubmitDtos());
        int totalDiscountAmount = 0;
        if (userId > 0) {
            log.info("action=createOrder, userId={}, message=\"사용자 로그인 상태, 총 할인 금액 계산 시작\"", userId);
            totalDiscountAmount = getTotalDiscountAmount(userId, orderSubmitDto, bookSimpleInfos, totalBookPrice);
            log.info("action=createOrder, userId={}, totalDiscountAmount={}, message=\"총 할인 금액 계산 완료\"", userId,
                    totalDiscountAmount);

        }
        int shippingFee = getShippingFee(totalBookPrice);
        int totalPavingPrice = getTotalPavingPrice(orderSubmitDto);
        String orderName = createOrderName(bookSimpleInfos.getIdTitlePriceDtos().get(0).getTitle(), bookIds.size());
        log.info(
                "action=createOrder, totalBookPrice={}, totalDiscountAmount={}, shippingFee={}, totalPavingPrice={}, orderName=\"{}\", message=\"주문 금액 및 이름 계산 완료\"",
                totalBookPrice, totalDiscountAmount, shippingFee, totalPavingPrice, orderName);

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
        log.info(
                "action=createOrder, orderId={}, userId={}, orderName=\"{}\", totalPrice={}, message=\"Orders 테이블에 주문 레코드 저장 완료\"",
                order.getId(), userId, order.getOrderName(), order.getTotalPrice());

        //------- Order_book table record add -------//
        List<OrderBook> orderBooks = new ArrayList<>();
        for (BookOrderSubmitDto bookOrderSubmitDto : orderSubmitDto.getBookOrderSubmitDtos()) {
            Paving paving = null;
            if (Objects.nonNull(bookOrderSubmitDto.getPavingId())) {
                paving = pavingRepository.findById(bookOrderSubmitDto.getPavingId()).orElse(null);
                log.debug("action=createOrder, bookId={}, pavingId={}, message=\"포장지 정보 조회 완료\"",
                        bookOrderSubmitDto.getBookId(), bookOrderSubmitDto.getPavingId());

            }
            OrderBook orderBook = new OrderBook(order, bookOrderSubmitDto.getBookId(), paving,
                    bookOrderSubmitDto.getBookQuantity(), bookIdPriceMap.get(bookOrderSubmitDto.getBookId()));
            orderBooks.add(orderBook);
        }
        orderBookRepository.saveAll(orderBooks);
        log.info("action=createOrder, orderId={}, orderBookCount={}, message=\"Order_book 테이블에 주문 도서 레코드 저장 완료\"",
                order.getId(), orderBooks.size());

        if (userId < 0) {
            log.info("action=createOrder, orderId={}, message=\"비회원 주문 처리 시작\"", order.getId());
            Guest guest = new Guest(orderSubmitDto.getGuestPassword(), order);
            guestRepository.save(guest);
            log.info("action=createOrder, orderId={}, message=\"Guest 테이블에 비회원 정보 저장 완료\"", order.getId());

        }

        if (userId > 0) {
            log.info("action=createOrder, orderId={}, userId={}, message=\"포인트 사용 내역 저장 시작\"", order.getId(), userId);
            PointHistoryCreateDTO pointHistoryCreateDTO = new PointHistoryCreateDTO(userId,
                    orderSubmitDto.getUsedPoints() == null ? 0 : orderSubmitDto.getUsedPoints(),
                    order.getTotalBookPrice(),
                    order.getId());
            pointHistoryCreateDtoRepository.save(pointHistoryCreateDTO);
            log.info("action=createOrder, orderId={}, userId={}, usedPoints={}, message=\"포인트 사용 내역 저장 완료\"",
                    order.getId(), userId, pointHistoryCreateDTO.usedPoints());

        }

        int payAmount = order.getTotalPrice();

        OrderInfoForPayment orderInfoForPayment = new OrderInfoForPayment(order.getId(), orderName, payAmount);
        log.info("action=createOrder, orderId={}, payAmount={}, message=\"주문 생성 로직 최종 완료, 결제 정보 반환\"", order.getId(),
                payAmount);
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

    private int getTotalBookPrice(BookIdTitlePriceListDto orderBookInfoListDto,
                                  List<BookOrderSubmitDto> idQuantityEtcDto) {
        int sum = 0;

        Map<Long, Integer> bookIdToQuantityMap = idQuantityEtcDto.stream()
                .collect(Collectors.toMap(
                        BookOrderSubmitDto::getBookId,
                        BookOrderSubmitDto::getBookQuantity
                ));

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
            Paving paving = pavingRepository.findById(dto.getPavingId())
                    .orElseThrow(() -> new RuntimeException("잘못된 포장지 아이디"));
            totalPavingPrice += paving.getPrice();
        }
        return totalPavingPrice;
    }

    private String createOrderName(String oneOfBookTitle, int orderItemCount) {
        if (orderItemCount > 1) {
            return String.format("%s 주문", oneOfBookTitle);
        }
        return String.format("%s외 %d권 주문", oneOfBookTitle, orderItemCount);
    }

    private int getTotalDiscountAmount(Long userId, OrderSubmitDto dto, BookIdTitlePriceListDto bookIdTitlePriceListDto,
                                       int totalBookPrice) {
        if (userId == null) {
            throw new OrderPaymentServiceException(OrderPaymentErrorCode.USER_NOT_FOUND);
        }
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

        List<TryApplyCouponsResponseDto> couponDiscountInfos = couponClient.getApplyCoupons(userId,
                new TryApplyCouponsRequestDto(couponIdBookIdMap)).getBody();//여기서 책아이디:가격 맵 생성
        Map<Long, Integer> bookIdPriceMap = new HashMap<>();

        for (BookIdTitlePriceDto idTitlePriceDto : bookIdTitlePriceListDto.getIdTitlePriceDtos()) {
            bookIdPriceMap.put(idTitlePriceDto.getBookId(), idTitlePriceDto.getSalesPrice());
        }
        //책에 적용할 쿠폰 할인가 합산
        List<UsedCouponInfo> usedCouponInfos = new ArrayList<>();
        for (TryApplyCouponsResponseDto discountInfo : couponDiscountInfos) {
            Long bookId = discountInfo.getBookId();
            UsedCouponInfo usedCouponInfo = new UsedCouponInfo();
            usedCouponInfo.setBookId(bookId);
            usedCouponInfo.setCouponId(discountInfo.getCouponId());
            usedCouponInfos.add(usedCouponInfo);
            if (Objects.nonNull(bookId)) {//책 각각에 적용
                Integer bookPrice = bookIdPriceMap.get(bookId);
                if (discountInfo.getDiscountType().equals(DiscountType.FIXED)) {
                    totalDiscountAmount += discountInfo.getDiscountValue();
                } else {
                    totalDiscountAmount += (bookPrice * ((double) discountInfo.getDiscountValue() / 100));
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

        if (Objects.nonNull(userId) && userId > 0 && Objects.nonNull(dto.getUsedPoints())) {
            totalDiscountAmount += dto.getUsedPoints();
        }

        return totalDiscountAmount;
    }

    //주문목록조회 (회원)
    @Transactional(readOnly = true)
    public Page<OrderLookupResponse> getOrdersByUser(Long userId, int page, int size) {
        log.info("action=getOrdersByUser, userId={}, page={}, size={}, message=\"사용자 주문 목록 조회 시작\"", userId, page,
                size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("orderAt").descending());

        return orderRepository.findAllByUserIdAndOrderStatusNot(userId, OrderStatus.FAILED, pageable)
                .map(order -> {
                    log.debug("action=getOrdersByUser, orderId={}, orderName=\"{}\", message=\"개별 주문 정보 처리 시작\"",
                            order.getId(), order.getOrderName());

                    OrderBook orderBook = orderBookRepository.findTop1ByOrder_IdOrderByObIdAsc(order.getId());
                    String thumbnailUrl = null;

                    if (orderBook != null) {
                        // 도서 썸네일 외부에서 받아오기
                        log.debug("action=getOrdersByUser, orderId={}, bookId={}, message=\"주문 도서 썸네일 조회 시작\"",
                                order.getId(), orderBook.getBookId());

                        OrderItemListDto oneBook = new OrderItemListDto(
                                List.of(new CartItem(orderBook.getBookId(), orderBook.getQuantity()))
                        );
                        OrderBookInfoListDto bookInfo = bookClient.getOrderBookInfos(oneBook);

                        if (!bookInfo.getOrderBookInfos().isEmpty()) {
                            thumbnailUrl = bookInfo.getOrderBookInfos().get(0).getThumbnailUrl();
                            log.debug(
                                    "action=getOrdersByUser, orderId={}, thumbnailUrl=\"{}\", message=\"주문 도서 썸네일 조회 완료\"",
                                    order.getId(), thumbnailUrl);
                        }
                    } else {
                        log.debug("action=getOrdersByUser, orderId={}, message=\"주문된 도서 정보 없음\"", order.getId());

                    }
                    log.debug("action=getOrdersByUser, orderId={}, message=\"개별 주문 정보 DTO 변환 완료\"", order.getId());
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

        log.info("action=getOrders, searchCriteria={}, page={}, size={}, message=\"주문 검색 목록 조회 완료\"", orderSearchDto.toString(), page, size);

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
        log.info("action=getOrder, orderId={}, userId={}, message=\"회원 주문 상세 조회 시작\"", orderId, userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderPaymentServiceException(OrderPaymentErrorCode.NOT_FOUND_ORDER_INFO));
        log.debug("action=getOrder, orderId={}, message=\"주문 엔티티 조회 완료\"", orderId);

        // 본인만 조회할수있도록
        if (!userId.equals(order.getUserId())) {
            throw new OrderPaymentServiceException(OrderPaymentErrorCode.YOU_CAN_ONLY_ACCESS_YOUR_ORDER);
        }
        log.debug("action=getOrder, orderId={}, userId={}, message=\"주문 소유자 검증 완료\"", orderId, userId);

        log.info("action=getOrder, orderId={}, userId={}, message=\"회원 주문 상세 조회 완료\"", orderId, userId);

        return orderDetailResponse(order);
    }

    //주문상세조회 (비회원)
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderForGuest(Long orderId, String password) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderPaymentServiceException(OrderPaymentErrorCode.NOT_FOUND_ORDER_INFO));

        Guest guest = guestRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new OrderPaymentServiceException(OrderPaymentErrorCode.NOT_FOUND_ORDER_INFO));

        if (!guest.getPassword().equals(password)) {
            throw new OrderPaymentServiceException(OrderPaymentErrorCode.NOT_FOUND_ORDER_INFO);
        }

        return orderDetailResponse(order);
    }

    //주문취소(결제취소) 회원
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        log.info("action=cancelOrder, orderId={}, userId={}, message=\"주문 취소 로직 시작\"", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderPaymentServiceException(OrderPaymentErrorCode.NOT_FOUND_ORDER_INFO));

        log.debug("action=cancelOrder, orderId={}, currentStatus={}, message=\"주문 엔티티 조회 완료\"", orderId, order.getOrderStatus());

        // 본인만 취소할수있도록 검사
        if (!userId.equals(order.getUserId())) {
            throw new OrderPaymentServiceException(OrderPaymentErrorCode.YOU_CAN_ONLY_ACCESS_YOUR_ORDER);
        }
        log.debug("action=cancelOrder, orderId={}, userId={}, message=\"주문 소유자 검증 완료\"", orderId, userId);

        // PAID(대기)에서만 취소가능
        if (order.getOrderStatus() != OrderStatus.PAID) {
            throw new OrderPaymentServiceException(OrderPaymentErrorCode.ONLY_CAN_CANCEL_WHEN_ORDER_PAID);
        }

        userClient.cancleOrderPointProcess(order.getId());
        log.info("action=cancelOrder, orderId={}, message=\"포인트 취소 처리 완료\"", order.getId());

        order.setOrderStatus(OrderStatus.CANCELLED);
        log.info("action=cancelOrder, orderId={}, newStatus={}, message=\"주문 상태 CANCELLED로 변경\"", order.getId(), OrderStatus.CANCELLED);

        log.info("action=cancelOrder, orderId={}, message=\"도서 재고 증가 처리 시작\"", order.getId());
        increaseBookStock(order);
        log.info("action=cancelOrder, orderId={}, message=\"도서 재고 증가 처리 완료\"", order.getId());

        int cancelMoney = order.getTotalPrice();
        log.info("action=cancelOrder, orderId={}, cancelMoney={}, message=\"결제 취소 서비스 호출 시작\"", order.getId(), cancelMoney);

        paymentService.paymentCancel(orderId, cancelMoney);
        log.info("action=cancelOrder, orderId={}, cancelMoney={}, message=\"결제 취소 서비스 호출 완료\"", order.getId(), cancelMoney);

        log.info("action=cancelOrder, orderId={}, userId={}, message=\"주문 취소 로직 최종 완료\"", orderId, userId);

    }


    //주문취소(결제취소) 비회원 -> 비회원은 order에 따로 userId가 없어서 조회하는것처럼 주문번호와 패스워드를 사용해서 주문취소
    @Transactional
    public void cancelGuestOrder(Long orderId, String password) {
        log.info("action=cancelGuestOrder, orderId={}, message=\"비회원 주문 취소 로직 시작\"", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderPaymentServiceException(OrderPaymentErrorCode.NOT_FOUND_ORDER_INFO));

        log.debug("action=cancelGuestOrder, orderId={}, message=\"주문 엔티티 조회 완료\"", orderId);

        Guest guest = guestRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new OrderPaymentServiceException(OrderPaymentErrorCode.NOT_FOUND_ORDER_INFO));

        log.debug("action=cancelGuestOrder, orderId={}, message=\"비회원 엔티티 조회 완료\"", orderId);

        if (!guest.getPassword().equals(password)) {
            throw new OrderPaymentServiceException(OrderPaymentErrorCode.NOT_FOUND_ORDER_INFO);
        }
        log.debug("action=cancelGuestOrder, orderId={}, message=\"비회원 비밀번호 검증 완료\"", orderId);

        // PAID(대기)에서만 취소가능
        if (order.getOrderStatus() != OrderStatus.PAID) {
            throw new OrderPaymentServiceException(OrderPaymentErrorCode.ONLY_CAN_CANCEL_WHEN_ORDER_PAID);
        }
        log.debug("action=cancelGuestOrder, orderId={}, message=\"주문 상태 (PAID) 검증 완료\"", orderId);

        order.setOrderStatus(OrderStatus.CANCELLED);
        log.info("action=cancelGuestOrder, orderId={}, newStatus={}, message=\"주문 상태 CANCELLED로 변경\"", orderId, OrderStatus.CANCELLED);

        log.info("action=cancelGuestOrder, orderId={}, message=\"도서 재고 증가 처리 시작\"", orderId);
        increaseBookStock(order);
        log.info("action=cancelGuestOrder, orderId={}, message=\"도서 재고 증가 처리 완료\"", orderId);

        int cancelMoney = order.getTotalPrice();
        log.info("action=cancelGuestOrder, orderId={}, cancelMoney={}, message=\"결제 취소 서비스 호출 시작\"", orderId, cancelMoney);
        paymentService.paymentCancel(orderId, cancelMoney);
        log.info("action=cancelGuestOrder, orderId={}, cancelMoney={}, message=\"결제 취소 서비스 호출 완료\"", orderId, cancelMoney);

        log.info("action=cancelGuestOrder, orderId={}, message=\"비회원 주문 취소 로직 최종 완료\"", orderId);

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
    public void updateStatus(Long userId, Long orderId, OrderStatus newStatus) {
        log.info("action=updateStatus, orderId={}, userId={}, newStatus={}, message=\"주문 상태 업데이트 로직 시작\"", orderId, userId, newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderPaymentServiceException(OrderPaymentErrorCode.NOT_FOUND_ORDER_INFO));

        log.debug("action=updateStatus, orderId={}, currentStatus={}, message=\"주문 엔티티 조회 완료\"", orderId, order.getOrderStatus());

        log.info("action=updateStatus, orderId={}, oldStatus={}, newStatus={}, message=\"주문 상태 변경\"", orderId, order.getOrderStatus(), newStatus);
        order.setOrderStatus(newStatus);

        if (newStatus.equals(OrderStatus.SHIPPING)) {
            order.setShippedAt(LocalDateTime.now());
            log.info("action=updateStatus, orderId={}, newStatus=SHIPPING, shippedAt={}, message=\"출고 시간 기록\"", orderId, order.getShippedAt());

        }
        log.info("action=updateStatus, orderId={}, userId={}, finalStatus={}, message=\"주문 상태 업데이트 로직 완료\"", orderId, userId, newStatus);

    }


    //반품 (회원)
    @Transactional
    public void refundOrder(Long userId, Long orderId, RefundReason reason) {
        log.info("action=refundOrder, orderId={}, userId={}, reason={}, message=\"회원 주문 환불 로직 시작\"", orderId, userId, reason);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderPaymentServiceException(OrderPaymentErrorCode.NOT_FOUND_ORDER_INFO));

        log.debug("action=refundOrder, orderId={}, currentStatus={}, message=\"주문 엔티티 조회 완료\"", orderId, order.getOrderStatus());

        // 본인만 반품할수있도록 검사
        if (!userId.equals(order.getUserId())) {
            throw new OrderPaymentServiceException(OrderPaymentErrorCode.YOU_CAN_ONLY_ACCESS_YOUR_ORDER);
        }
        log.debug("action=refundOrder, orderId={}, userId={}, message=\"주문 소유자 검증 완료\"", orderId, userId);

        // 반품 가능 상태 확인 (예: 배송완료 상태만 반품 허용)
        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new OrderPaymentServiceException(OrderPaymentErrorCode.ONLY_CAN_REFUND_WHEN_ORDER_COMPLETED);
        }
        log.debug("action=refundOrder, orderId={}, message=\"주문 상태 (COMPLETED) 검증 완료\"", orderId);

        LocalDateTime deliveryAt = order.getShippedAt(); // 출고일
        int refundPoint = 0;

        if (reason.equals(RefundReason.DAMAGED)) {
            if (deliveryAt == null || deliveryAt.plusDays(30).isBefore(LocalDateTime.now())) {
                throw new OrderPaymentServiceException(OrderPaymentErrorCode.DAMAGED_ITEM_REFUND_RESTRICTION);
            }
            refundPoint = order.getTotalPrice() + order.getTotalDiscountAmount(); //제품불량은 전부 환불
        } else if (reason.equals(RefundReason.JUST)) {
            if (deliveryAt == null || deliveryAt.plusDays(10).isBefore(LocalDateTime.now())) {
                throw new OrderPaymentServiceException(OrderPaymentErrorCode.UNUSED_ITEM_REFUND_RESTRICTION);
            }
            refundPoint = order.getTotalBookPrice() + order.getTotalDiscountAmount(); //배송비 제외 환불

        }

        //환불 포인트 값 보내주기
        if (refundPoint > 0) {
            userClient.refundPoint(orderId, refundPoint);
        }

        // 상태 변경
        order.setOrderStatus(OrderStatus.RETURNED);
        log.info("action=refundOrder, orderId={}, newStatus={}, message=\"주문 상태 RETURNED로 변경\"", orderId, OrderStatus.RETURNED);

        log.info("action=refundOrder, orderId={}, message=\"도서 재고 증가 처리 시작\"", orderId);
        increaseBookStock(order);
        log.info("action=refundOrder, orderId={}, message=\"도서 재고 증가 처리 완료\"", orderId);

        log.info("action=refundOrder, orderId={}, userId={}, message=\"회원 주문 환불 로직 최종 완료\"", orderId, userId);

    }

    //반품(비회원)
    @Transactional
    public void refundGuestOrder(Long orderId, String password, RefundReason reason) {
        log.info("action=refundGuestOrder, orderId={}, reason={}, message=\"비회원 주문 환불 로직 시작\"", orderId, reason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderPaymentServiceException(OrderPaymentErrorCode.NOT_FOUND_ORDER_INFO));

        log.debug("action=refundGuestOrder, orderId={}, currentStatus={}, message=\"주문 엔티티 조회 완료\"", orderId, order.getOrderStatus());

        Guest guest = guestRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new OrderPaymentServiceException(OrderPaymentErrorCode.NOT_FOUND_ORDER_INFO));

        if (!guest.getPassword().equals(password)) {
            throw new OrderPaymentServiceException(OrderPaymentErrorCode.NOT_FOUND_ORDER_INFO);
        }

        int refundMoney = 0;

        // 반품 가능 상태 확인 (예: 배송완료 상태만 반품 허용)
        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new OrderPaymentServiceException(OrderPaymentErrorCode.ONLY_CAN_REFUND_WHEN_ORDER_COMPLETED);
        }
        LocalDateTime shippedAt = order.getShippedAt(); //출고일

        if (reason.equals(RefundReason.DAMAGED)) {
            if (shippedAt == null || shippedAt.plusDays(30).isBefore(LocalDateTime.now())) {
                throw new OrderPaymentServiceException(OrderPaymentErrorCode.DAMAGED_ITEM_REFUND_RESTRICTION);
            }
            refundMoney = order.getTotalPrice(); //제품불량은 전부 환불

        } else if (reason.equals(RefundReason.JUST)) {
            if (shippedAt == null || shippedAt.plusDays(10).isBefore(LocalDateTime.now())) {
                throw new OrderPaymentServiceException(OrderPaymentErrorCode.UNUSED_ITEM_REFUND_RESTRICTION);
            }
            refundMoney = order.getTotalBookPrice(); //단순변심은 배송비제외
        }

        order.setOrderStatus(OrderStatus.RETURNED);
        log.info("action=refundGuestOrder, orderId={}, newStatus={}, message=\"주문 상태 RETURNED로 변경\"", orderId, OrderStatus.RETURNED);

        log.info("action=refundGuestOrder, orderId={}, message=\"도서 재고 증가 처리 시작\"", orderId);
        increaseBookStock(order);
        log.info("action=refundGuestOrder, orderId={}, message=\"도서 재고 증가 처리 완료\"", orderId);

        log.info("action=refundGuestOrder, orderId={}, refundMoney={}, message=\"결제 취소 서비스 호출 시작\"", orderId, refundMoney);
        paymentService.paymentCancel(orderId, refundMoney);
        log.info("action=refundGuestOrder, orderId={}, refundMoney={}, message=\"결제 취소 서비스 호출 완료\"", orderId, refundMoney);

        log.info("action=refundGuestOrder, orderId={}, message=\"비회원 주문 환불 로직 최종 완료\"", orderId);


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
