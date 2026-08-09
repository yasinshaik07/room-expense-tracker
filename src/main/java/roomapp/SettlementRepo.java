package roomapp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SettlementRepo extends JpaRepository<Settlement, Long> {

    List<Settlement> findByRoomCodeOrderByPaidAtDesc(
            String roomCode
    );

    List<Settlement> findByRoomCodeAndPaidAtBetweenOrderByPaidAtDesc(
            String roomCode,
            LocalDateTime start,
            LocalDateTime end
    );
}