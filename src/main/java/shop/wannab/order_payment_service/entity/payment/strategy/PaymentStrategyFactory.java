package shop.wannab.order_payment_service.entity.payment.strategy;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class PaymentStrategyFactory {
    private final Map<String, PaymentStrategy> strategies = new HashMap<>();

    public PaymentStrategyFactory(Set<PaymentStrategy> strategySet) {
        for (PaymentStrategy strategy : strategySet) {
            strategies.put(strategy.getProviderName(), strategy);
        }
    }

    public PaymentStrategy getStrategy(String providerName) {
        PaymentStrategy strategy = strategies.get(providerName.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("지원하지 않는 결제 대행사: " + providerName);
        }
        return strategy;
    }
}
