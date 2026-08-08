package roomapp;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long paidById;

    private String paidByName;

    private String itemName;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    private LocalDate expenseDate;

    private String splitMode;

    @Column(length = 2000)
    private String sharedMemberIds;

    public Expense() {
    }

    public Long getId() {
        return id;
    }

    public Long getPaidById() {
        return paidById;
    }

    public String getPaidByName() {
        return paidByName;
    }

    public String getItemName() {
        return itemName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public String getSplitMode() {
        return splitMode;
    }

    public String getSharedMemberIds() {
        return sharedMemberIds;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPaidById(Long paidById) {
        this.paidById = paidById;
    }

    public void setPaidByName(String paidByName) {
        this.paidByName = paidByName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public void setSplitMode(String splitMode) {
        this.splitMode = splitMode;
    }

    public void setSharedMemberIds(String sharedMemberIds) {
        this.sharedMemberIds = sharedMemberIds;
    }
}