package shop.wannab.order_payment_service.config;

import javax.sql.DataSource;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "dbcp2.datasource")
public class DataSourceConfig {

    private String driverClassName;
    private String url;
    private String username;
    private String password;
    private Integer maxIdle;
    private Integer maxTotal;
    private Integer initialSize;
    private Integer minIdle;

    @Bean
    public DataSource dataSource() {
        BasicDataSource basicDataSource = new BasicDataSource();

        basicDataSource.setDriverClassName(driverClassName);
        basicDataSource.setUrl(url);
        basicDataSource.setUsername(username);
        basicDataSource.setPassword(password);
        basicDataSource.setMaxIdle(maxIdle);
        basicDataSource.setMaxTotal(maxTotal);
        basicDataSource.setInitialSize(initialSize);
        basicDataSource.setMinIdle(minIdle);

        basicDataSource.setValidationQuery("SELECT 1");
        basicDataSource.setTestOnReturn(true);
        basicDataSource.setTestOnBorrow(false);
        basicDataSource.setTestWhileIdle(true);

        return basicDataSource;
    }
}
