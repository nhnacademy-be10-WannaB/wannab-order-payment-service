package shop.wannab.order_payment_service.hardcoding;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// TODO: 임시하드코딩, 외부API연동시 수정예정 (작동확인을 위해 book데이터가 필요해서 작성함)
@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initBooks(BookRepository bookRepository) {
        return args -> {
            bookRepository.save(new Book(1L, "도서 제목1", 15000, null));
            bookRepository.save(new Book(2L, "도서 제목2", 20000, null));
        };
    }
}