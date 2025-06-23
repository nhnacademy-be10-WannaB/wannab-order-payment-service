package shop.wannab.order_payment_service.service;

import io.micrometer.common.util.StringUtils;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import shop.wannab.order_payment_service.entity.RefundReason;
import shop.wannab.order_payment_service.entity.WrappingPaper;
import shop.wannab.order_payment_service.entity.dto.*;

import java.util.List;
import shop.wannab.order_payment_service.exception.WrappingPaperNotFoundException;
import shop.wannab.order_payment_service.repository.GuestRepository;
import shop.wannab.order_payment_service.repository.OrderBookRepository;
import shop.wannab.order_payment_service.repository.OrderReopsitory;
import shop.wannab.order_payment_service.repository.WrappingPaperRepository;
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

    //배송지 정책 계산
    public int getShippingFee(int totalBookPrice) {
        DeliveryPolicy deliveryPolicy = deliveryPolicyService.findApplicablePolicy(totalBookPrice);
        int shippingFee = deliveryPolicy.getFee();
        return shippingFee;
    }


    //주문생성
    @Transactional
    public OrderResponse createOrder(OrderRequest request, Long userId) {
        Order order = new Order();
        order.setOrderAt(LocalDateTime.now());
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
                    .filter(req -> req.getBookId().equals(bookInfo.getBookId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("주문서 도서정보 누락"));

            OrderBook orderBook = new OrderBook();
            orderBook.setOrder(order);
            orderBook.setBookId(bookInfo.getBookId());
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


    //주문목록조회 (회원)
    @Transactional(readOnly = true)
    public Page<OrderListResponse> getOrdersByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderAt").descending());

        return orderReopsitory.findAllByUserId(userId, pageable)
                .map(order -> new OrderListResponse(
                        order.getId(),
                        order.getOrderAt(),
                        order.getOrderStatus(),
                        order.getDeliveryAt(),
                        order.getTotalPrice()
                ));
    }

    // 쇼핑몰 주문 전체 조회 (관리자용)
    @Transactional(readOnly = true)
    public Page<OrderListResponse> getOrders(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderAt").descending());

        // ADMIN 확인
        String role = userClient.getUserRole(userId);

        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new IllegalArgumentException("관리자만 주문 전체 조회 가능");
        }

        // 주문자 id나 이름도 띄우면 좋을듯
        // -> 비회원이면 이메일 띄우고

        return orderReopsitory.findAll(pageable)
                .map(order -> new OrderListResponse(
                        order.getId(),
                        order.getOrderAt(), //주문일시
                        order.getOrderStatus(), //주문상태
                        order.getDeliveryAt(), // 배송일(또는 null)
                        order.getTotalPrice() //최종 결제금액
                ));
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
                order.getOrderAt(), // TODO: 결제일시로 바꾸기
                order.getOrderStatus(),
                order.getId().toString(), // 송장번호 = 주문번호(임시)
                order.getTotalPrice()
        );
    }

    //주문상세조회 (회원)
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(Long orderId, Long userId) {
        Order order = orderReopsitory.findById(orderId).orElseThrow(() -> new IllegalArgumentException("주문정보없음"));

        // 본인만 조회할수있도록
        if (!userId.equals(order.getUserId())) {
            throw new IllegalArgumentException("본인 주문만 반품가능");
        }

        return orderDetailResponse(order);
    }

    //주문상세조회 (비회원)
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderForGuest(Long orderId, String password) {
        Order order = orderReopsitory.findById(orderId)
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

        Order order = orderReopsitory.findById(orderId).orElseThrow(() -> new IllegalArgumentException("주문번호를 찾을수 없음"));

        // 본인만 취소할수있도록 검사
        if (!userId.equals(order.getUserId())) {
            throw new IllegalArgumentException("본인 주문만 취소가능");
        }


        // PENDING(대기)에서만 취소가능
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("현재 주문 상태에서는 취소할 수 없습니다: " + order.getOrderStatus());
        }

        //포인트반환 로직추가(회원인 경우만)
        if (userId != null && userId > 0) {
            int refundPoint = order.getTotalPrice() + order.getTotalDiscount(); //주문전 취소라 모든 금액을 돌려줌
            userClient.refundPoint(userId, userId, refundPoint);
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
    }

    //주문취소(결제취소) 비회원 -> 비회원은 order에 따로 userId가 없어서 조회하는것처럼 주문번호와 패스워드를 사용해서 주문취소
    @Transactional
    public void cancelGuestOrder(Long orderId, String password){
        Order order = orderReopsitory.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

        Guest guest = guestRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new IllegalArgumentException("비회원 주문자 정보가 없습니다."));

        if (!guest.getPassword().equals(password)) {
            throw new IllegalArgumentException("주문번호 또는 비밀번호가 일치하지 않습니다.");
        }

        // PENDING(대기)에서만 취소가능
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("현재 주문 상태에서는 취소할 수 없습니다: " + order.getOrderStatus());
        }

        //비회원은 환불로직을 어떻게 해야할지 고민

        order.setOrderStatus(OrderStatus.CANCELLED);
    }


    //주문상태변경 (관리자)
    @Transactional
    public void updateStatus(Long userId, Long orderId, OrderStatus newStatus){
        Order order = orderReopsitory.findById(orderId).orElseThrow(() -> new IllegalArgumentException("주문번호를 찾을수 없음"));

        // ADMIN 확인
        String role = userClient.getUserRole(userId);

        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new IllegalArgumentException("관리자만 주문 전체 조회 가능");
        }

        order.setOrderStatus(newStatus);
    }


    //반품 reason부분은 추후에 enum으로 수정

    //반품 (회원)
    @Transactional
    public void refundOrder(Long userId, Long orderId, RefundReason reason){
        Order order = orderReopsitory.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다"));

        // 본인만 반품할수있도록 검사
        if (!userId.equals(order.getUserId())) {
            throw new IllegalArgumentException("본인 주문만 반품가능");
        }

        // 반품 가능 상태 확인 (예: 배송완료 상태만 반품 허용)
        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new IllegalStateException("현재 상태에서는 반품할 수 없습니다.");
        }


        LocalDateTime deliveryAt = order.getDeliveryAt(); // 출고일
        int refundPoint = 0;

        if(reason.equals(RefundReason.DAMAGED)){
            if (deliveryAt == null || deliveryAt.plusDays(30).isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("제품불량은 출고일로부터 30일 이내만 반품이 가능합니다.");
            }
            refundPoint = order.getTotalPrice() + order.getTotalDiscount(); //제품불량은 전부 환불
        }else if(reason.equals(RefundReason.JUST)){
            if(deliveryAt == null || deliveryAt.plusDays(10).isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("미사용 제품은 출고일로부터 10일 이내만 반품이 가능합니다.");
            }
            refundPoint = order.getTotalBookPrice() + order.getTotalDiscount(); //배송비 제외 환불

        }

        //환불 포인트 값 보내주기
        if (refundPoint > 0) {
            userClient.refundPoint(userId, userId, refundPoint);
        }

        // 상태 변경
        order.setOrderStatus(OrderStatus.RETURNED);
    }

    //반품(비회원)
    @Transactional
    public void refundGuestOrder(Long orderId, String password, RefundReason reason){
        Order order = orderReopsitory.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

        Guest guest = guestRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new IllegalArgumentException("비회원 주문자 정보가 없습니다."));

        if (!guest.getPassword().equals(password)) {
            throw new IllegalArgumentException("주문번호 또는 비밀번호가 일치하지 않습니다.");
        }

        // 반품 가능 상태 확인 (예: 배송완료 상태만 반품 허용)
        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new IllegalStateException("현재 상태에서는 반품할 수 없습니다.");
        }

        LocalDateTime deliveryAt = order.getDeliveryAt(); //출고일

        if(reason.equals(RefundReason.DAMAGED)){
            if (deliveryAt == null || deliveryAt.plusDays(30).isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("제품불량은 출고일로부터 30일 이내만 반품이 가능합니다.");
            }
        }else if(reason.equals(RefundReason.JUST)){
            if(deliveryAt == null || deliveryAt.plusDays(10).isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("미사용 제품은 출고일로부터 10일 이내만 반품이 가능합니다.");
            }
        }
        order.setOrderStatus(OrderStatus.RETURNED);
    }






}
