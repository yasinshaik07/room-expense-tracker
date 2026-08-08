package roomapp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepo extends JpaRepository<Expense, Long> {

    List<Expense> findAllByOrderByExpenseDateDescIdDesc();

    List<Expense> findByExpenseDate(LocalDate date);

    List<Expense> findByExpenseDateBetween(
            LocalDate start,
            LocalDate end
    );
}