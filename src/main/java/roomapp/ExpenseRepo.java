package roomapp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepo extends JpaRepository<Expense, Long> {

    List<Expense> findByRoomCodeAndDeletedFalseOrderByExpenseDateDescIdDesc(
            String roomCode
    );

    List<Expense> findByRoomCodeAndDeletedFalseAndExpenseDate(
            String roomCode,
            LocalDate date
    );

    List<Expense> findByRoomCodeAndDeletedFalseAndExpenseDateBetween(
            String roomCode,
            LocalDate start,
            LocalDate end
    );

    List<Expense> findByRoomCodeAndDeletedTrueOrderByDeletedAtDesc(
            String roomCode
    );
}