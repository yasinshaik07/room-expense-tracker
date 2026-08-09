package roomapp;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SettlementRepo extends JpaRepository<Settlement, Long> {

    List<Settlement> findByRoomCodeOrderByPaidAtDesc(String roomCode);
}