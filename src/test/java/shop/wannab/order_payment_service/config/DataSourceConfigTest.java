package shop.wannab.order_payment_service.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

class DataSourceConfigTest {

    @Test
    void dataSourceBeanIsConfiguredCorrectly() {
        // given
        DataSourceConfig config = new DataSourceConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setUrl("jdbc:h2:mem:testdb");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaxIdle(10);
        config.setMaxTotal(20);
        config.setInitialSize(5);
        config.setMinIdle(3);
        config.setBetweenEvictionMillis(1L);
        config.setMinEvictableIdleMillis(10L);
        config.setNumTestsPerEvictionRun(2);

        // when
        BasicDataSource ds = (BasicDataSource) config.dataSource();

        // then: Getter도 호출하여 커버리지 올리기
        assertThat(config.getDriverClassName()).isEqualTo("org.h2.Driver");
        assertThat(config.getUrl()).isEqualTo("jdbc:h2:mem:testdb");
        assertThat(config.getUsername()).isEqualTo("sa");
        assertThat(config.getPassword()).isEqualTo("");
        assertThat(config.getMaxIdle()).isEqualTo(10);
        assertThat(config.getMaxTotal()).isEqualTo(20);
        assertThat(config.getInitialSize()).isEqualTo(5);
        assertThat(config.getMinIdle()).isEqualTo(3);
        assertThat(config.getBetweenEvictionMillis()).isEqualTo(1L);
        assertThat(config.getMinEvictableIdleMillis()).isEqualTo(10L);
        assertThat(config.getNumTestsPerEvictionRun()).isEqualTo(2);

        // then: Bean 설정 확인
        assertThat(ds.getDriverClassName()).isEqualTo("org.h2.Driver");
        assertThat(ds.getUrl()).isEqualTo("jdbc:h2:mem:testdb");
        assertThat(ds.getUsername()).isEqualTo("sa");
        assertThat(ds.getMaxIdle()).isEqualTo(10);
        assertThat(ds.getMaxTotal()).isEqualTo(20);
        assertThat(ds.getInitialSize()).isEqualTo(5);
        assertThat(ds.getMinIdle()).isEqualTo(3);
        assertThat(ds.getTimeBetweenEvictionRunsMillis()).isEqualTo(60_000L);
        assertThat(ds.getMinEvictableIdleTimeMillis()).isEqualTo(600_000L);
        assertThat(ds.getNumTestsPerEvictionRun()).isEqualTo(2);
    }
}