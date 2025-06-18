package shop.wannab.order_payment_service.hardcoding;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// TODO: 임시하드코딩, 외부API연동시 수정예정
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String name;

    private int points;


    public void deductPoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("차감할 포인트는 음수일 수 없습니다: " + amount);
        }
        if (this.points < amount) {
            throw new IllegalArgumentException("포인트부족");
        }
        this.points -= amount;
    }
}
