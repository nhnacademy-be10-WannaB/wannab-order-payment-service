package shop.wannab.order_payment_service.repository.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import shop.wannab.order_payment_service.entity.OrderStatus;
import shop.wannab.order_payment_service.entity.QOrder;
import shop.wannab.order_payment_service.entity.dto.OrderLookupResponse;
import shop.wannab.order_payment_service.entity.dto.OrderSearchDto;

@RequiredArgsConstructor
public class OrderQueryRepositoryImpl implements OrderQueryRepository{

    private final JPAQueryFactory queryFactory;

    //주문검색
    @Override
    public Page<OrderLookupResponse> searchOrders(OrderSearchDto orderSearchDto,
                                                  Pageable pageable){

        QOrder order = QOrder.order;

        BooleanBuilder builder = new BooleanBuilder();

        Long orderId = orderSearchDto.getOrderId();
        String orderName = orderSearchDto.getOrderName();
        OrderStatus orderStatus = orderSearchDto.getOrderStatus();
        LocalDate from = orderSearchDto.getFrom();
        LocalDate to = orderSearchDto.getTo();

        if(orderId != null){
            builder.and(order.id.eq(orderId));
        }
        if (StringUtils.hasText(orderName)) {
            builder.and(order.orderName.containsIgnoreCase(orderName));
        }
        if (orderStatus != null) {
            builder.and(order.orderStatus.eq(orderStatus));
        }

        if (from != null) {
            builder.and(order.orderAt.goe(from.atStartOfDay()));
        }
        if (to != null) {
            builder.and(order.orderAt.loe(to.atTime(LocalTime.MAX)));
        }

        //계산로직
        NumberExpression<Integer> totalPriceExpr = order.totalBookPrice
                .add(order.totalPavingPrice)
                .add(order.shippingFee)
                .subtract(order.totalDiscountAmount);


        List<OrderLookupResponse> list = queryFactory.select(Projections.constructor(OrderLookupResponse.class,
                order.id,
                order.orderName,
                order.orderAt,
                order.orderStatus,
                order.shippedAt,
                totalPriceExpr))
                .from(order)
                .where(builder)
                .orderBy(order.orderAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                queryFactory
                    .select(order.count())
                    .from(order)
                    .where(builder)
                    .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(list, pageable, total);
    }
}
