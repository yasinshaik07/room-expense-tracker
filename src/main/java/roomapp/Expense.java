package roomapp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomCode;

    private Long paidById;

    private String paidByName;

    private String itemName;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    private LocalDate expenseDate;

    private String splitMode;

    @Column(length = 2000)
    private String sharedMemberIds;

    private boolean deleted = false;

    private String deletedBy;

    private LocalDateTime deletedAt;

    public Expense() {
    }

    public Long getId() {
        return id;
    }

    public String getRoomCode() {
        return roomCode;
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

    public boolean isDeleted() {
        return deleted;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
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

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}