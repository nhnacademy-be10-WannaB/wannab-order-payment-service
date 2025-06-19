package shop.wannab.order_payment_service.hardcoding;

import org.springframework.data.jpa.repository.JpaRepository;

// TODO: 임시하드코딩, 외부API연동시 수정예정
public interface BookRepository extends JpaRepository<Book, Long> {
}
