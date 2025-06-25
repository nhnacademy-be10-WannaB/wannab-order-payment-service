package shop.wannab.order_payment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients
@EnableDiscoveryClient
@EnableScheduling	//주문상태 <- 일정시간지나면 배송완료로 변경하기위해 추가
@SpringBootApplication
public class OrderPaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderPaymentServiceApplication.class, args);
	}

}
