package roomapp;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlements")
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roomCode;

    @Column(nullable = false)
    private Long fromMemberId;

    @Column(nullable = false)
    private String fromName;

    @Column(nullable = false)
    private Long toMemberId;

    @Column(nullable = false)
    private String toName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime paidAt;

    private String recordedBy;

    public Settlement() {
    }

    public Long getId() {
        return id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public Long getFromMemberId() {
        return fromMemberId;
    }

    public String getFromName() {
        return fromName;
    }

    public Long getToMemberId() {
        return toMemberId;
    }

    public String getToName() {
        return toName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public String getRecordedBy() {
        return recordedBy;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public void setFromMemberId(Long fromMemberId) {
        this.fromMemberId = fromMemberId;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public void setToMemberId(Long toMemberId) {
        this.toMemberId = toMemberId;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }
}