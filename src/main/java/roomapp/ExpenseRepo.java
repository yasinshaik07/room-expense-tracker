package roomapp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepo extends JpaRepository<Expense, Long> {

    List<Expense> findByRoomCodeAndDeletedFalseOrderByExpenseDateDescIdDesc(
            String roomCode
    );

    List<Expense> findByRoomCodeAndDeletedTrueOrderByDeletedAtDesc(
            String roomCode
    );

    List<Expense> findByRoomCodeAndDeletedFalseAndExpenseDate(
            String roomCode,
            LocalDate expenseDate
    );

    List<Expense> findByRoomCodeAndDeletedFalseAndExpenseDateBetween(
            String roomCode,
            LocalDate startDate,
            LocalDate endDate
    );
}