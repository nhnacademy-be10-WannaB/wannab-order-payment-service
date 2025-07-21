package shop.wannab.order_payment_service.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class QuerydslConfigTest {

    @Test
    void jpaQueryFactoryBeanShouldBeCreated() {
        EntityManager entityManager = mock(EntityManager.class);
        QuerydslConfig config = new QuerydslConfig();

        JPAQueryFactory factory = config.jpaQueryFactory(entityManager);

        assertThat(factory).isNotNull();
    }
}