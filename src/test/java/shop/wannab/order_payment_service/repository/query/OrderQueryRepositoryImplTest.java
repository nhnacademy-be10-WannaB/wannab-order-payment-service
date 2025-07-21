package shop.wannab.order_payment_service.repository.query;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.EntityPathBase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import shop.wannab.order_payment_service.entity.dto.OrderLookupResponse;
import shop.wannab.order_payment_service.entity.dto.OrderSearchDto;
import shop.wannab.order_payment_service.repository.OrderRepository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;


import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderQueryRepositoryImplTest {

    private EntityManager entityManager;
    private JPAQueryFactory queryFactory;
    private OrderQueryRepositoryImpl orderQueryRepository;

    @BeforeEach
    void setUp() {
        entityManager = mock(EntityManager.class);
        queryFactory = mock(JPAQueryFactory.class);
        orderQueryRepository = new OrderQueryRepositoryImpl(queryFactory);
    }


}
