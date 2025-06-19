package shop.wannab.order_payment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration.class
})
@EnableDiscoveryClient
@EnableFeignClients
public class OrderPaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderPaymentServiceApplication.class, args);
	}

}
