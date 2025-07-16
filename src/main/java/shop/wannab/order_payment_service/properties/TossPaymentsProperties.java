package shop.wannab.order_payment_service.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter @Setter
@Component
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "toss.payments")
public class TossPaymentsProperties {
    private String secretKey;
    private String prefix;
}
