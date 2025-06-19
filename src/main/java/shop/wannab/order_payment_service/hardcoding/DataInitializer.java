package shop.wannab.order_payment_service.hardcoding;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// TODO: 임시하드코딩, 외부API연동시 수정예정 (작동확인을 위해 book데이터가 필요해서 작성함)
@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initBooks(BookRepository bookRepository,
                                       UserRepository userRepository,
                                       AddressRepository addressRepository) {
        return args -> {
            bookRepository.save(new Book(1L, "도서 제목1", 15000, null));
            bookRepository.save(new Book(2L, "도서 제목2", 20000, null));

            // User 데이터 생성
            User user1 = new User(1L, "정민수", 10000, "kinggodminsu@naver.com");
            userRepository.save(user1);

            // Address 데이터 생성
            addressRepository.save(new Address(1L, "대한민국 567", user1));
            addressRepository.save(new Address(2L, "대한민국 111117", user1));
        };
    }
}